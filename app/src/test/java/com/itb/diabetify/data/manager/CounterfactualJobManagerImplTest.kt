package com.itb.diabetify.data.manager

import com.itb.diabetify.data.remote.counterfactual.CounterfactualApiService
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobStatusData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobStatusResponse
import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CounterfactualJobManagerImplTest {

    @Test
    fun `pollJobStatus emits pending progress and completed states`() = runTest {
        val api = FakeCounterfactualApiService(
            statusResponses = ArrayDeque(
                listOf(
                    statusResponse("submitted"),
                    statusResponse("processing"),
                    statusResponse("completed")
                )
            )
        )
        val manager = CounterfactualJobManagerImpl(api)

        val states = manager.pollJobStatus("job-1", pollingIntervalMs = 1L).toList()

        assertEquals(
            listOf(
                CounterfactualJobStatus.Pending,
                CounterfactualJobStatus.Pending,
                CounterfactualJobStatus.InProgress,
                CounterfactualJobStatus.Completed
            ),
            states
        )
    }

    @Test
    fun `pollJobStatus emits failed when response data is missing`() = runTest {
        val api = FakeCounterfactualApiService(
            statusResponses = ArrayDeque(listOf(CounterfactualJobStatusResponse(data = null, message = "bad payload")))
        )
        val manager = CounterfactualJobManagerImpl(api)

        val states = manager.pollJobStatus("job-2", pollingIntervalMs = 1L).toList()

        assertEquals(2, states.size)
        assertEquals(CounterfactualJobStatus.Pending, states[0])
        assertEquals(
            CounterfactualJobStatus.Failed("bad payload"),
            states[1]
        )
    }

    @Test
    fun `pollJobStatus emits failed when api throws`() = runTest {
        val api = FakeCounterfactualApiService(
            statusError = IllegalStateException("backend down")
        )
        val manager = CounterfactualJobManagerImpl(api)

        val states = manager.pollJobStatus("job-3", pollingIntervalMs = 1L).toList()

        assertEquals(2, states.size)
        assertEquals(CounterfactualJobStatus.Pending, states[0])
        assertEquals(
            CounterfactualJobStatus.Failed("backend down"),
            states[1]
        )
    }

    @Test
    fun `pollJobStatus treats infeasible as completed terminal state`() = runTest {
        val api = FakeCounterfactualApiService(
            statusResponses = ArrayDeque(listOf(statusResponse("infeasible")))
        )
        val manager = CounterfactualJobManagerImpl(api)

        val states = manager.pollJobStatus("job-4", pollingIntervalMs = 1L).toList()

        assertEquals(
            listOf(
                CounterfactualJobStatus.Pending,
                CounterfactualJobStatus.Completed
            ),
            states
        )
    }

    @Test
    fun `pollJobStatus maps cancelled state to failed with backend message`() = runTest {
        val api = FakeCounterfactualApiService(
            statusResponses = ArrayDeque(
                listOf(
                    CounterfactualJobStatusResponse(
                        data = CounterfactualJobStatusData(
                            jobId = "job-5",
                            jobStatus = "cancelled",
                            message = "Job dibatalkan"
                        ),
                        status = "success"
                    )
                )
            )
        )
        val manager = CounterfactualJobManagerImpl(api)

        val states = manager.pollJobStatus("job-5", pollingIntervalMs = 1L).toList()

        assertEquals(2, states.size)
        assertEquals(CounterfactualJobStatus.Pending, states[0])
        assertEquals(CounterfactualJobStatus.Failed("Job dibatalkan"), states[1])
    }

    private fun statusResponse(status: String): CounterfactualJobStatusResponse {
        return CounterfactualJobStatusResponse(
            data = CounterfactualJobStatusData(
                jobId = "job-1",
                jobStatus = status
            ),
            status = "success"
        )
    }
}

private class FakeCounterfactualApiService(
    private val statusResponses: ArrayDeque<CounterfactualJobStatusResponse> = ArrayDeque(),
    private val statusError: Exception? = null
) : CounterfactualApiService {
    override suspend fun submitCounterfactual(request: CounterfactualRequest): CounterfactualJobResponse {
        return CounterfactualJobResponse(data = CounterfactualJobData(jobId = "job-unused"))
    }

    override suspend fun getCounterfactualJobStatus(jobId: String): CounterfactualJobStatusResponse {
        statusError?.let { throw it }
        return statusResponses.removeFirst()
    }

    override suspend fun getCounterfactualJobResult(jobId: String): CounterfactualJobResultResponse {
        return CounterfactualJobResultResponse()
    }
}

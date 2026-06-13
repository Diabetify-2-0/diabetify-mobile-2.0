package com.itb.diabetify.data.repository

import com.itb.diabetify.data.remote.counterfactual.CounterfactualApiService
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualConstraints
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualFeatureSet
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualInstance
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobStatusResponse
import com.itb.diabetify.domain.manager.CounterfactualJobManager
import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterfactualRepositoryImplTest {

    @Test
    fun `startCounterfactualJob returns success resource`() = runTest {
        val request = testRequest()
        val api = FakeCounterfactualApiService(
            submitResponse = CounterfactualJobResponse(
                data = CounterfactualJobData(jobId = "job-123")
            )
        )
        val repository = CounterfactualRepositoryImpl(api, FakeJobManager())

        val result = repository.startCounterfactualJob(request)

        assertTrue(result is Resource.Success)
        assertEquals("job-123", (result as Resource.Success).data?.data?.jobId)
    }

    @Test
    fun `startCounterfactualJob returns error resource on io exception`() = runTest {
        val repository = CounterfactualRepositoryImpl(
            FakeCounterfactualApiService(submitError = IOException("network down")),
            FakeJobManager()
        )

        val result = repository.startCounterfactualJob(testRequest())

        assertTrue(result is Resource.Error)
        assertEquals("network down", (result as Resource.Error).message)
    }

    @Test
    fun `pollCounterfactualJob delegates to manager`() = runTest {
        val manager = FakeJobManager(
            statuses = flowOf(CounterfactualJobStatus.Pending, CounterfactualJobStatus.Completed)
        )
        val repository = CounterfactualRepositoryImpl(FakeCounterfactualApiService(), manager)

        val statuses = repository.pollCounterfactualJob("job-77", 1500L).toList()

        assertEquals("job-77", manager.lastJobId)
        assertEquals(1500L, manager.lastPollingIntervalMs)
        assertEquals(
            listOf(CounterfactualJobStatus.Pending, CounterfactualJobStatus.Completed),
            statuses
        )
    }

    @Test
    fun `getCounterfactualJobResult returns success resource`() = runTest {
        val repository = CounterfactualRepositoryImpl(
            FakeCounterfactualApiService(
                resultResponse = CounterfactualJobResultResponse(
                    data = CounterfactualJobResultData(jobId = "job-123")
                )
            ),
            FakeJobManager()
        )

        val result = repository.getCounterfactualJobResult("job-123")

        assertTrue(result is Resource.Success)
        assertEquals("job-123", (result as Resource.Success).data?.data?.jobId)
    }

    @Test
    fun `getCounterfactualJobResult returns error resource on io exception`() = runTest {
        val repository = CounterfactualRepositoryImpl(
            FakeCounterfactualApiService(resultError = IOException("timeout")),
            FakeJobManager()
        )

        val result = repository.getCounterfactualJobResult("job-123")

        assertTrue(result is Resource.Error)
        assertEquals("timeout", (result as Resource.Error).message)
    }

    private fun testRequest(): CounterfactualRequest {
        return CounterfactualRequest(
            instance = CounterfactualInstance(
                features = CounterfactualFeatureSet(
                    age = 45,
                    smokingStatus = 0,
                    isCholesterol = 1,
                    isMacrosomicBaby = 0,
                    moderatePhysicalActivityFrequency = 1,
                    isBloodline = 0,
                    brinkmanIndex = 0,
                    bmi = 31.2,
                    isHypertension = 1
                )
            ),
            constraints = CounterfactualConstraints(
                mutableAllowed = listOf("BMI", "moderate_physical_activity_frequency")
            )
        )
    }
}

private class FakeJobManager(
    private val statuses: Flow<CounterfactualJobStatus> = flowOf(CounterfactualJobStatus.Completed)
) : CounterfactualJobManager {
    var lastJobId: String? = null
    var lastPollingIntervalMs: Long? = null

    override suspend fun pollJobStatus(jobId: String, pollingIntervalMs: Long): Flow<CounterfactualJobStatus> {
        lastJobId = jobId
        lastPollingIntervalMs = pollingIntervalMs
        return statuses
    }
}

private class FakeCounterfactualApiService(
    private val submitResponse: CounterfactualJobResponse = CounterfactualJobResponse(),
    private val resultResponse: CounterfactualJobResultResponse = CounterfactualJobResultResponse(),
    private val submitError: Exception? = null,
    private val resultError: Exception? = null
) : CounterfactualApiService {
    override suspend fun submitCounterfactual(request: CounterfactualRequest): CounterfactualJobResponse {
        submitError?.let { throw it }
        return submitResponse
    }

    override suspend fun getCounterfactualJobStatus(jobId: String): CounterfactualJobStatusResponse {
        return CounterfactualJobStatusResponse()
    }

    override suspend fun getCounterfactualJobResult(jobId: String): CounterfactualJobResultResponse {
        resultError?.let { throw it }
        return resultResponse
    }
}

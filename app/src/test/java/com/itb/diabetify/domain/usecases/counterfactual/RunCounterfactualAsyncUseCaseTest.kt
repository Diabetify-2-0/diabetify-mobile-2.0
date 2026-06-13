package com.itb.diabetify.domain.usecases.counterfactual

import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualConstraints
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualFeatureSet
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualInstance
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultResponse
import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import com.itb.diabetify.domain.repository.CounterfactualRepository
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RunCounterfactualAsyncUseCaseTest {

    @Test
    fun `invoke returns async result with job flow when job id exists`() = runTest {
        val repository = FakeCounterfactualRepository(
            startResource = Resource.Success(
                CounterfactualJobResponse(
                    data = CounterfactualJobData(jobId = "job-123")
                )
            ),
            statuses = flowOf(CounterfactualJobStatus.Pending, CounterfactualJobStatus.Completed)
        )
        val useCase = RunCounterfactualAsyncUseCase(repository)

        val result = useCase(testRequest(), pollingIntervalMs = 1500L)

        assertEquals("job-123", result.jobId)
        assertNotNull(result.jobStatusFlow)
        assertNull(result.error)
        assertEquals("job-123", repository.lastPolledJobId)
        assertEquals(1500L, repository.lastPollingIntervalMs)
    }

    @Test
    fun `invoke returns error when response omits job id`() = runTest {
        val repository = FakeCounterfactualRepository(
            startResource = Resource.Success(CounterfactualJobResponse(data = CounterfactualJobData(jobId = null)))
        )
        val useCase = RunCounterfactualAsyncUseCase(repository)

        val result = useCase(testRequest())

        assertEquals("Gagal mendapatkan job ID dari respons counterfactual", result.error)
        assertNull(result.jobId)
        assertNull(result.jobStatusFlow)
    }

    @Test
    fun `invoke returns repository error message`() = runTest {
        val repository = FakeCounterfactualRepository(
            startResource = Resource.Error("service unavailable")
        )
        val useCase = RunCounterfactualAsyncUseCase(repository)

        val result = useCase(testRequest())

        assertEquals("service unavailable", result.error)
        assertNull(result.jobId)
        assertNull(result.jobStatusFlow)
    }

    @Test
    fun `invoke handles loading resource as generic startup failure`() = runTest {
        val repository = FakeCounterfactualRepository(
            startResource = Resource.Loading()
        )
        val useCase = RunCounterfactualAsyncUseCase(repository)

        val result = useCase(testRequest())

        assertEquals("Terjadi kesalahan saat memulai counterfactual", result.error)
        assertNull(result.jobId)
        assertNull(result.jobStatusFlow)
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

private class FakeCounterfactualRepository(
    private val startResource: Resource<CounterfactualJobResponse>,
    private val statuses: Flow<CounterfactualJobStatus> = flowOf(CounterfactualJobStatus.Completed)
) : CounterfactualRepository {
    var lastPolledJobId: String? = null
    var lastPollingIntervalMs: Long? = null

    override suspend fun startCounterfactualJob(request: CounterfactualRequest): Resource<CounterfactualJobResponse> {
        return startResource
    }

    override suspend fun pollCounterfactualJob(
        jobId: String,
        pollingIntervalMs: Long
    ): Flow<CounterfactualJobStatus> {
        lastPolledJobId = jobId
        lastPollingIntervalMs = pollingIntervalMs
        return statuses
    }

    override suspend fun getCounterfactualJobResult(jobId: String): Resource<CounterfactualJobResultResponse> {
        return Resource.Success(CounterfactualJobResultResponse())
    }
}

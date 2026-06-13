package com.itb.diabetify.util

import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualConstraints
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualFeatureSet
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualInstance
import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobData
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResponse
import com.itb.diabetify.data.remote.counterfactual.response.CounterfactualJobResultResponse
import com.itb.diabetify.domain.manager.CounterfactualJobManager
import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import com.itb.diabetify.domain.repository.CounterfactualRepository
import com.itb.diabetify.domain.usecases.counterfactual.CounterfactualUseCases
import com.itb.diabetify.domain.usecases.counterfactual.GetCounterfactualJobResultUseCase
import com.itb.diabetify.domain.usecases.counterfactual.RunCounterfactualAsyncUseCase
import com.itb.diabetify.domain.usecases.counterfactual.StartCounterfactualJobUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CounterfactualExtensionsTest {

    @Test
    fun `handleAsyncCounterfactual invokes pending progress and completed callbacks`() = runTest {
        val events = mutableListOf<String>()
        val useCases = buildUseCases(
            FakeCounterfactualRepository(
                startResource = Resource.Success(
                    CounterfactualJobResponse(data = CounterfactualJobData(jobId = "job-123"))
                ),
                statuses = flowOf(
                    CounterfactualJobStatus.Pending,
                    CounterfactualJobStatus.InProgress,
                    CounterfactualJobStatus.Completed
                )
            )
        )

        useCases.handleAsyncCounterfactual(
            scope = this,
            request = testRequest(),
            pollingIntervalMs = 200L,
            onPending = { events += "pending" },
            onProgress = { events += "progress" },
            onCompleted = { jobId -> events += "completed:$jobId" },
            onFailed = { error -> events += "failed:$error" }
        )

        assertEquals(
            listOf("pending", "progress", "completed:job-123"),
            events
        )
    }

    @Test
    fun `handleAsyncCounterfactual invokes failed callback when async start fails`() = runTest {
        val events = mutableListOf<String>()
        val useCases = buildUseCases(
            FakeCounterfactualRepository(
                startResource = Resource.Error("backend unavailable")
            )
        )

        useCases.handleAsyncCounterfactual(
            scope = this,
            request = testRequest(),
            onFailed = { error -> events += error }
        )

        assertEquals(listOf("backend unavailable"), events)
    }

    @Test
    fun `handleAsyncCounterfactual invokes failed callback when terminal status fails`() = runTest {
        val events = mutableListOf<String>()
        val useCases = buildUseCases(
            FakeCounterfactualRepository(
                startResource = Resource.Success(
                    CounterfactualJobResponse(data = CounterfactualJobData(jobId = "job-555"))
                ),
                statuses = flowOf(CounterfactualJobStatus.Failed("job failed"))
            )
        )

        useCases.handleAsyncCounterfactual(
            scope = this,
            request = testRequest(),
            onFailed = { error -> events += error }
        )

        assertEquals(listOf("job failed"), events)
    }

    private fun buildUseCases(repository: CounterfactualRepository): CounterfactualUseCases {
        return CounterfactualUseCases(
            startCounterfactualJob = StartCounterfactualJobUseCase(repository),
            runCounterfactualAsync = RunCounterfactualAsyncUseCase(repository),
            getCounterfactualJobResult = GetCounterfactualJobResultUseCase(repository)
        )
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
    private val statuses: Flow<CounterfactualJobStatus> = flowOf(CounterfactualJobStatus.Completed),
    private val resultResource: Resource<CounterfactualJobResultResponse> = Resource.Success(
        CounterfactualJobResultResponse()
    )
) : CounterfactualRepository {
    override suspend fun startCounterfactualJob(request: CounterfactualRequest): Resource<CounterfactualJobResponse> {
        return startResource
    }

    override suspend fun pollCounterfactualJob(
        jobId: String,
        pollingIntervalMs: Long
    ): Flow<CounterfactualJobStatus> {
        return statuses
    }

    override suspend fun getCounterfactualJobResult(jobId: String): Resource<CounterfactualJobResultResponse> {
        return resultResource
    }
}

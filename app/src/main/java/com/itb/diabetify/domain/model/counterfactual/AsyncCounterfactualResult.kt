package com.itb.diabetify.domain.model.counterfactual

import com.itb.diabetify.domain.manager.CounterfactualJobStatus
import kotlinx.coroutines.flow.Flow

data class AsyncCounterfactualResult(
    val jobId: String? = null,
    val jobStatusFlow: Flow<CounterfactualJobStatus>? = null,
    val error: String? = null
)

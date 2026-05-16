package com.itb.diabetify.domain.usecases.counterfactual

import com.itb.diabetify.domain.repository.CounterfactualRepository

class GetCounterfactualJobResultUseCase(
    private val repository: CounterfactualRepository
) {
    suspend operator fun invoke(jobId: String) = repository.getCounterfactualJobResult(jobId)
}

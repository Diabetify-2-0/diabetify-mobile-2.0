package com.itb.diabetify.domain.usecases.counterfactual

import com.itb.diabetify.data.remote.counterfactual.request.CounterfactualRequest
import com.itb.diabetify.domain.repository.CounterfactualRepository

class StartCounterfactualJobUseCase(
    private val repository: CounterfactualRepository
) {
    suspend operator fun invoke(request: CounterfactualRequest) = repository.startCounterfactualJob(request)
}

package com.itb.diabetify.domain.usecases.counterfactual

data class CounterfactualUseCases(
    val startCounterfactualJob: StartCounterfactualJobUseCase,
    val runCounterfactualAsync: RunCounterfactualAsyncUseCase,
    val getCounterfactualJobResult: GetCounterfactualJobResultUseCase
)

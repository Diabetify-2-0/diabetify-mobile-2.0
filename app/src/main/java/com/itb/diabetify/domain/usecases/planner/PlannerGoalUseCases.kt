package com.itb.diabetify.domain.usecases.planner

data class PlannerGoalUseCases(
    val savePlannerGoal: SavePlannerGoalUseCase,
    val getActivePlannerGoal: GetActivePlannerGoalUseCase,
    val refreshPlannerGoal: RefreshPlannerGoalUseCase,
    val completePlannerGoal: CompletePlannerGoalUseCase,
    val clearPlannerGoal: ClearPlannerGoalUseCase,
    val markPlannerCheckIn: MarkPlannerCheckInUseCase,
    val getPlannerCheckIns: GetPlannerCheckInsUseCase,
    val clearPlannerCheckIns: ClearPlannerCheckInsUseCase,
    val refreshPlannerCheckIns: RefreshPlannerCheckInsUseCase,
    val refreshPlannerCheckInHistory: RefreshPlannerCheckInHistoryUseCase,
    val recordPlannerCheckIn: RecordPlannerCheckInUseCase,
    val getPlannerCheckInHistory: GetPlannerCheckInHistoryUseCase
)

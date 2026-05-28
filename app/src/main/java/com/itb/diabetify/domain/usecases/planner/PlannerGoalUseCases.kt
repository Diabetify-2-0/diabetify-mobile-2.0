package com.itb.diabetify.domain.usecases.planner

data class PlannerGoalUseCases(
    val savePlannerGoal: SavePlannerGoalUseCase,
    val getActivePlannerGoal: GetActivePlannerGoalUseCase,
    val getPlannerGoalHistory: GetPlannerGoalHistoryUseCase,
    val refreshPlannerGoal: RefreshPlannerGoalUseCase,
    val refreshPlannerGoalHistory: RefreshPlannerGoalHistoryUseCase,
    val clearPlannerGoal: ClearPlannerGoalUseCase,
    val markPlannerCheckIn: MarkPlannerCheckInUseCase,
    val getPlannerCheckIns: GetPlannerCheckInsUseCase,
    val refreshPlannerCheckIns: RefreshPlannerCheckInsUseCase,
    val refreshPlannerCheckInHistory: RefreshPlannerCheckInHistoryUseCase,
    val recordPlannerCheckIn: RecordPlannerCheckInUseCase,
    val getPlannerCheckInHistory: GetPlannerCheckInHistoryUseCase
)

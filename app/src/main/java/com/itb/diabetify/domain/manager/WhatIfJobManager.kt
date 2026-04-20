package com.itb.diabetify.domain.manager

import kotlinx.coroutines.flow.Flow

interface WhatIfJobManager {
    suspend fun pollJobStatus(jobId: String, pollingIntervalMs: Long = 2000L): Flow<WhatIfJobStatus>
    suspend fun cancelJob(jobId: String)
}

sealed class WhatIfJobStatus {
    object Pending : WhatIfJobStatus()
    data class InProgress(val progress: Int) : WhatIfJobStatus()
    object Completed : WhatIfJobStatus()
    data class Failed(val error: String) : WhatIfJobStatus()
}

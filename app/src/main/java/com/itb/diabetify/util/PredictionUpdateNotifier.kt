package com.itb.diabetify.util

object PredictionUpdateNotifier {
    private val listeners = mutableSetOf<() -> Unit>()
    private val updatingListeners = mutableSetOf<(Boolean) -> Unit>()

    var isPredictionUpdating: Boolean = false
        private set

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    fun addUpdatingListener(listener: (Boolean) -> Unit) {
        updatingListeners.add(listener)
        listener(isPredictionUpdating)
    }

    fun removeUpdatingListener(listener: (Boolean) -> Unit) {
        updatingListeners.remove(listener)
    }

    fun notifyPredictionUpdateStarted() {
        isPredictionUpdating = true
        updatingListeners.forEach { it(true) }
    }

    fun notifyPredictionUpdateFinished() {
        isPredictionUpdating = false
        updatingListeners.forEach { it(false) }
    }

    fun notifyPredictionUpdated() {
        listeners.forEach { it() }
    }

    fun clearAllListeners() {
        listeners.clear()
        updatingListeners.clear()
        isPredictionUpdating = false
    }
}

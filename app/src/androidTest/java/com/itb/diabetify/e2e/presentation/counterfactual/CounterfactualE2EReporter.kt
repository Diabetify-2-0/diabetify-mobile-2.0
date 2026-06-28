package com.itb.diabetify.e2e.presentation.counterfactual

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CounterfactualE2EReporter(
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
) {
    private val records = mutableListOf<ScenarioRecord>()

    data class ScenarioRecord(
        val id: String,
        val name: String,
        val status: String,
        val durationMs: Long,
        val detail: String
    )

    fun scenario(
        id: String,
        name: String,
        block: () -> String = { "OK" }
    ) {
        val startedAt = SystemClock.elapsedRealtime()
        try {
            val detail = block()
            val record = ScenarioRecord(
                id = id,
                name = name,
                status = "PASSED",
                durationMs = SystemClock.elapsedRealtime() - startedAt,
                detail = detail
            )
            records += record
            Log.i("CounterfactualE2E", record.toLogLine())
            println("COUNTERFACTUAL_E2E_SCENARIO ${record.toLogLine()}")
        } catch (throwable: Throwable) {
            val record = ScenarioRecord(
                id = id,
                name = name,
                status = "FAILED",
                durationMs = SystemClock.elapsedRealtime() - startedAt,
                detail = throwable.message ?: throwable::class.java.simpleName
            )
            records += record
            Log.e("CounterfactualE2E", record.toLogLine(), throwable)
            println("COUNTERFACTUAL_E2E_SCENARIO ${record.toLogLine()}")
        }
    }

    fun writeAndAssert(minimumSuccessRate: Double = 0.90): File {
        val total = records.size
        val passed = records.count { it.status == "PASSED" }
        val successRate = if (total == 0) 0.0 else passed.toDouble() / total.toDouble()
        val report = buildJsonReport(total = total, passed = passed, successRate = successRate)
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val reportDir = File(context.filesDir, "e2e-reports").apply { mkdirs() }
        val reportFile = File(reportDir, "counterfactual-rm5-$timestamp.json")
        reportFile.writeText(report)

        println("COUNTERFACTUAL_E2E_REPORT_PATH=${reportFile.absolutePath}")
        println("COUNTERFACTUAL_E2E_SUMMARY passed=$passed total=$total successRate=${"%.2f".format(successRate * 100)}%")

        check(successRate >= minimumSuccessRate) {
            val failedScenarios = records
                .filter { it.status == "FAILED" }
                .joinToString(separator = "\n") { record ->
                    "${record.id} ${record.name}: ${record.detail}"
                }
            "Counterfactual RM5 E2E success rate ${"%.2f".format(successRate * 100)}% " +
                "is below target ${"%.2f".format(minimumSuccessRate * 100)}%. " +
                "Report: ${reportFile.absolutePath}\nFailed scenarios:\n$failedScenarios"
        }
        return reportFile
    }

    private fun buildJsonReport(total: Int, passed: Int, successRate: Double): String {
        val scenariosJson = records.joinToString(separator = ",\n") { record ->
            """
            {
              "id": "${record.id.escapeJson()}",
              "name": "${record.name.escapeJson()}",
              "status": "${record.status}",
              "duration_ms": ${record.durationMs},
              "detail": "${record.detail.escapeJson()}"
            }
            """.trimIndent()
        }

        return """
        {
          "suite": "Counterfactual RM5 End-to-End",
          "generated_at_epoch_ms": ${System.currentTimeMillis()},
          "success_criteria": {
            "minimum_success_rate": 0.9,
            "minimum_passed_scenarios": 11,
            "total_scenarios": 12
          },
          "summary": {
            "passed": $passed,
            "failed": ${total - passed},
            "total": $total,
            "success_rate": $successRate
          },
          "scenarios": [
        $scenariosJson
          ]
        }
        """.trimIndent()
    }

    private fun ScenarioRecord.toLogLine(): String {
        return "id=$id status=$status durationMs=$durationMs detail=${detail.replace('\n', ' ')}"
    }

    private fun String.escapeJson(): String {
        return buildString {
            for (char in this@escapeJson) {
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }
    }
}

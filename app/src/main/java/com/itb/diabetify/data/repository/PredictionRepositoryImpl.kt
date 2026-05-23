package com.itb.diabetify.data.repository

import com.itb.diabetify.data.remote.prediction.PredictionApiService
import com.itb.diabetify.data.remote.prediction.response.GetPredictionResponse
import com.itb.diabetify.data.remote.prediction.response.GetPredictionScoreResponse
import com.itb.diabetify.data.remote.prediction.response.PredictionJobResponse
import com.itb.diabetify.domain.manager.PredictionJobManager
import com.itb.diabetify.domain.manager.PredictionJobStatus
import com.itb.diabetify.domain.manager.PredictionManager
import com.itb.diabetify.domain.manager.TokenManager
import com.itb.diabetify.domain.model.Prediction
import com.itb.diabetify.domain.repository.PredictionRepository
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okio.IOException
import retrofit2.HttpException

class PredictionRepositoryImpl (
    private val predictionApiService: PredictionApiService,
    private val tokenManager: TokenManager,
    private val predictionManager: PredictionManager,
    private val predictionJobManager: PredictionJobManager,
) : PredictionRepository {
    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }

    override suspend fun predict(): Resource<Unit> {
        return try {
            val jobResponse = predictionApiService.predict()
            val jobId = jobResponse.data?.jobId
                ?: return Resource.Error(jobResponse.message ?: "Gagal mendapatkan job ID prediksi")

            val jobStatusFlow = predictionJobManager.pollJobStatus(jobId)
            when (val status = jobStatusFlow.first { it is PredictionJobStatus.Completed || it is PredictionJobStatus.Failed }) {
                is PredictionJobStatus.Completed -> {
                    fetchLatestPrediction()
                    Resource.Success(Unit)
                }
                is PredictionJobStatus.Failed -> Resource.Error(status.error)
                else -> Resource.Error("Unknown prediction job status")
            }
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun startPredictionJob(): Resource<PredictionJobResponse> {
        return try {
            val response = predictionApiService.predict()
            Resource.Success(response)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun pollPredictionJob(jobId: String, pollingIntervalMs: Long): Flow<PredictionJobStatus> {
        return predictionJobManager.pollJobStatus(jobId, pollingIntervalMs)
    }

    override suspend fun explainPrediction(): Resource<Unit> {
        return try {
            predictionApiService.explainPrediction()
            fetchLatestPrediction()
            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun fetchLatestPrediction(): Resource<Unit> {
        return try {
            val response = predictionApiService.getPrediction(1)
            val latestPrediction = response.data.filterNotNull().firstOrNull()
            if (latestPrediction == null) {
                predictionManager.savePrediction(emptyPrediction())
                return Resource.Success(Unit)
            }

            val ageContribution = signedContribution(latestPrediction.ageContribution, latestPrediction.ageImpact)
            val bmiContribution = signedContribution(latestPrediction.bmiContribution, latestPrediction.bmiImpact)
            val brinkmanContribution = signedContribution(
                latestPrediction.brinkmanScoreContribution,
                latestPrediction.brinkmanScoreImpact
            )
            val isHypertensionContribution = signedContribution(
                latestPrediction.isHypertensionContribution,
                latestPrediction.isHypertensionImpact
            )
            val isCholesterolContribution = signedContribution(
                latestPrediction.isCholesterolContribution,
                latestPrediction.isCholesterolImpact
            )
            val isBloodlineContribution = signedContribution(
                latestPrediction.isBloodlineContribution,
                latestPrediction.isBloodlineImpact
            )
            val isMacrosomicBabyContribution = signedContribution(
                latestPrediction.isMacrosomicBabyContribution,
                latestPrediction.isMacrosomicBabyImpact
            )
            val smokingStatusContribution = signedContribution(
                latestPrediction.smokingStatusContribution,
                latestPrediction.smokingStatusImpact
            )
            val physicalActivityContribution = signedContribution(
                latestPrediction.physicalActivityFrequencyContribution,
                latestPrediction.physicalActivityFrequencyImpact
            )

            predictionManager.savePrediction(
                Prediction(
                    riskScore = (latestPrediction.riskScore ?: 0.0) * 100,
                    predictionSummary = latestPrediction.predictionSummary.orEmpty().ifEmpty {
                        "Prediksi ini didasarkan pada faktor-faktor risiko yang telah Anda berikan. Silakan periksa faktor-faktor tersebut untuk memahami lebih lanjut tentang risiko diabetes Anda."
                    },
                    age = latestPrediction.age ?: 0,
                    ageContribution = ageContribution * 100,
                    ageExplanation = latestPrediction.ageExplanation.orEmpty().ifEmpty {
                        "Usia Anda adalah faktor utama dalam menilai risiko diabetes Anda. Seiring bertambahnya usia, kemampuan tubuh untuk mengelola gula darah dapat berubah, yang sering kali meningkatkan kerentanan Anda."
                    },
                    bmi = latestPrediction.bmi ?: 0.0,
                    bmiContribution = bmiContribution * 100,
                    bmiExplanation = latestPrediction.bmiExplanation.orEmpty().ifEmpty {
                        "Indeks Massa Tubuh (IMT) Anda adalah ukuran lemak tubuh berdasarkan tinggi dan berat badan. IMT yang lebih tinggi sangat terkait dengan peningkatan risiko terkena diabetes tipe 2."
                    },
                    brinkmanScore = latestPrediction.brinkmanScore ?: 0,
                    brinkmanScoreContribution = brinkmanContribution * 100,
                    brinkmanScoreExplanation = latestPrediction.brinkmanScoreExplanation.orEmpty().ifEmpty {
                        "Indeks Brinkman mengukur total paparan Anda terhadap rokok seumur hidup. Skor yang lebih tinggi, yang menandakan kebiasaan merokok yang lebih intens atau lama, berkontribusi pada risiko diabetes yang lebih besar."
                    },
                    isHypertension = latestPrediction.isHypertension ?: false,
                    isHypertensionContribution = isHypertensionContribution * 100,
                    isHypertensionExplanation = latestPrediction.isHypertensionExplanation.orEmpty().ifEmpty {
                        "Faktor ini menunjukkan apakah Anda telah didiagnosis menderita tekanan darah tinggi (hipertensi). Memiliki hipertensi sangat erat kaitannya dengan resistensi insulin, yang meningkatkan peluang Anda terkena diabetes."
                    },
                    isCholesterol = latestPrediction.isCholesterol ?: false,
                    isCholesterolContribution = isCholesterolContribution * 100,
                    isCholesterolExplanation = latestPrediction.isCholesterolExplanation.orEmpty().ifEmpty {
                        "Faktor ini mencatat apakah Anda memiliki kadar kolesterol tinggi. Kolesterol tinggi sering kali menyertai faktor risiko lain dan dapat berkontribusi pada kondisi yang mengarah ke diabetes tipe 2."
                    },
                    isBloodline = latestPrediction.isBloodline ?: false,
                    isBloodlineContribution = isBloodlineContribution * 100,
                    isBloodlineExplanation = latestPrediction.isBloodlineExplanation.orEmpty().ifEmpty {
                        "Faktor ini mencerminkan apakah Anda memiliki riwayat diabetes dalam keluarga langsung (orang tua). Adanya faktor keturunan merupakan salah satu hal yang diketahui dapat meningkatkan risiko Anda."
                    },
                    isMacrosomicBaby = latestPrediction.isMacrosomicBaby ?: 0,
                    isMacrosomicBabyContribution = isMacrosomicBabyContribution * 100,
                    isMacrosomicBabyExplanation = latestPrediction.isMacrosomicBabyExplanation.orEmpty().ifEmpty {
                        "Faktor ini menunjukkan apakah Anda pernah melahirkan bayi dengan berat di atas 4 kg. Riwayat semacam ini bisa menjadi tanda adanya diabetes gestasional saat kehamilan, yang membuat Anda lebih rentan terkena diabetes tipe 2 di kemudian hari."
                    },
                    smokingStatus = latestPrediction.smokingStatus.orEmpty().ifBlank { "0" },
                    smokingStatusContribution = smokingStatusContribution * 100,
                    avgSmokeCount = latestPrediction.avgSmokeCount ?: 0,
                    smokingStatusExplanation = latestPrediction.smokingStatusExplanation.orEmpty().ifEmpty {
                        "Faktor ini menjelaskan status merokok Anda saat ini, baik perokok aktif, mantan perokok, atau tidak pernah merokok. Merokok dapat meningkatkan peradangan dan resistensi insulin, sehingga menaikkan risiko diabetes Anda secara keseluruhan."
                    },
                    physicalActivityFrequency = latestPrediction.physicalActivityFrequency ?: 0,
                    physicalActivityFrequencyContribution = physicalActivityContribution * 100,
                    physicalActivityFrequencyExplanation = latestPrediction.physicalActivityFrequencyExplanation.orEmpty().ifEmpty {
                        "Faktor ini mengukur seberapa sering Anda melakukan aktivitas fisik dalam seminggu. Olahraga teratur membantu mengontrol berat badan dan meningkatkan cara tubuh menggunakan insulin, sehingga menurunkan risiko diabetes Anda."
                    },
                    createdAt = latestPrediction.createdAt.orEmpty()
                )
            )

            Resource.Success(Unit)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun fetchPredictionByDate(
        startDate: String,
        endDate: String
    ): Resource<GetPredictionResponse> {
        return try {
            val response = predictionApiService.getPredictionByDate(startDate, endDate)
            if (response.data.isEmpty()) {
                Resource.Success(GetPredictionResponse(emptyList(), "No predictions found", "success"))
            } else {
                Resource.Success(response)
            }
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun fetchPredictionScoreByDate(
        startDate: String,
        endDate: String
    ): Resource<GetPredictionScoreResponse> {
        return try {
            val response = predictionApiService.getPredictionScoreByDate(startDate, endDate)
            Resource.Success(response)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override fun getLatestPrediction(): Flow<Prediction?> {
        return predictionManager.getLatestPrediction()
    }

    private fun signedContribution(value: Double?, impact: Int?): Double {
        val contribution = value ?: 0.0
        return if (impact == 1) contribution else -contribution
    }

    private fun emptyPrediction(): Prediction {
        return Prediction(
            riskScore = 0.0,
            predictionSummary = "",
            age = 0,
            ageContribution = 0.0,
            ageExplanation = "",
            bmi = 0.0,
            bmiContribution = 0.0,
            bmiExplanation = "",
            brinkmanScore = 0,
            brinkmanScoreContribution = 0.0,
            brinkmanScoreExplanation = "",
            isHypertension = false,
            isHypertensionContribution = 0.0,
            isHypertensionExplanation = "",
            isCholesterol = false,
            isCholesterolContribution = 0.0,
            isCholesterolExplanation = "",
            isBloodline = false,
            isBloodlineContribution = 0.0,
            isBloodlineExplanation = "",
            isMacrosomicBaby = 0,
            isMacrosomicBabyContribution = 0.0,
            isMacrosomicBabyExplanation = "",
            smokingStatus = "0",
            smokingStatusContribution = 0.0,
            smokingStatusExplanation = "",
            avgSmokeCount = 0,
            physicalActivityFrequency = 0,
            physicalActivityFrequencyContribution = 0.0,
            physicalActivityFrequencyExplanation = "",
            createdAt = ""
        )
    }
}

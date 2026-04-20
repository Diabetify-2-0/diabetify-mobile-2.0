package com.itb.diabetify.data.repository

import com.itb.diabetify.data.remote.prediction.PredictionApiService
import com.itb.diabetify.data.remote.prediction.request.WhatIfPredictionRequest
import com.itb.diabetify.data.remote.prediction.response.GetPredictionResponse
import com.itb.diabetify.data.remote.prediction.response.GetPredictionScoreResponse
import com.itb.diabetify.data.remote.prediction.response.PredictionJobResponse
import com.itb.diabetify.data.remote.prediction.response.WhatIfPredictionResponse
import com.itb.diabetify.data.remote.prediction.response.WhatIfJobResponse
import com.itb.diabetify.data.remote.prediction.response.WhatIfJobResultResponse
import com.itb.diabetify.domain.manager.PredictionJobManager
import com.itb.diabetify.domain.manager.PredictionJobStatus
import com.itb.diabetify.domain.manager.WhatIfJobManager
import com.itb.diabetify.domain.manager.WhatIfJobStatus
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
    private val whatIfJobManager: WhatIfJobManager,
) : PredictionRepository {
    override suspend fun getToken(): String? {
        return tokenManager.getToken()
    }

    override suspend fun predict(): Resource<Unit> {
        return try {
            val jobResponse = predictionApiService.predict()
            
            val jobStatusFlow = predictionJobManager.pollJobStatus(jobResponse.data.jobId)
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
            if (response.data.isEmpty()) {
                predictionManager.savePrediction(
                    Prediction(
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
                        createdAt = response.data.firstOrNull()?.createdAt ?: ""
                    )
                )
                return Resource.Success(Unit)
            }

            response.data.firstOrNull()?.let { prediction ->
                val ageContribution = if (prediction.ageImpact == 1) prediction.ageContribution else -prediction.ageContribution
                val bmiContribution = if (prediction.bmiImpact == 1) prediction.bmiContribution else -prediction.bmiContribution
                val brinkmanContribution = if (prediction.brinkmanScoreImpact == 1)
                    prediction.brinkmanScoreContribution else -prediction.brinkmanScoreContribution
                val isHypertensionContribution = if (prediction.isHypertensionImpact == 1)
                    prediction.isHypertensionContribution else -prediction.isHypertensionContribution
                val isCholesterolContribution = if (prediction.isCholesterolImpact == 1)
                    prediction.isCholesterolContribution else -prediction.isCholesterolContribution
                val isBloodlineContribution = if (prediction.isBloodlineImpact == 1)
                    prediction.isBloodlineContribution else -prediction.isBloodlineContribution
                val isMacrosomicBabyContribution = if (prediction.isMacrosomicBabyImpact == 1)
                    prediction.isMacrosomicBabyContribution else -prediction.isMacrosomicBabyContribution
                val smokingStatusContribution = if (prediction.smokingStatusImpact == 1)
                    prediction.smokingStatusContribution else -prediction.smokingStatusContribution
                val physicalActivityContribution = if (prediction.physicalActivityFrequencyImpact == 1)
                    prediction.physicalActivityFrequencyContribution else -prediction.physicalActivityFrequencyContribution

                predictionManager.savePrediction(
                    Prediction(
                        riskScore = prediction.riskScore * 100,
                        predictionSummary = prediction.predictionSummary.ifEmpty {
                            "Prediksi ini didasarkan pada faktor-faktor risiko yang telah Anda berikan. Silakan periksa faktor-faktor tersebut untuk memahami lebih lanjut tentang risiko diabetes Anda."
                        },
                        age = prediction.age,
                        ageContribution = ageContribution * 100,
                        ageExplanation = prediction.ageExplanation.ifEmpty {
                            "Usia Anda adalah faktor utama dalam menilai risiko diabetes Anda. Seiring bertambahnya usia, kemampuan tubuh untuk mengelola gula darah dapat berubah, yang sering kali meningkatkan kerentanan Anda."
                        },
                        bmi = prediction.bmi,
                        bmiContribution = bmiContribution * 100,
                        bmiExplanation = prediction.bmiExplanation.ifEmpty {
                            "Indeks Massa Tubuh (IMT) Anda adalah ukuran lemak tubuh berdasarkan tinggi dan berat badan. IMT yang lebih tinggi sangat terkait dengan peningkatan risiko terkena diabetes tipe 2."
                        },
                        brinkmanScore = prediction.brinkmanScore,
                        brinkmanScoreContribution = brinkmanContribution * 100,
                        brinkmanScoreExplanation = prediction.brinkmanScoreExplanation.ifEmpty {
                            "Indeks Brinkman mengukur total paparan Anda terhadap rokok seumur hidup. Skor yang lebih tinggi, yang menandakan kebiasaan merokok yang lebih intens atau lama, berkontribusi pada risiko diabetes yang lebih besar."
                        },
                        isHypertension = prediction.isHypertension,
                        isHypertensionContribution = isHypertensionContribution * 100,
                        isHypertensionExplanation = prediction.isHypertensionExplanation.ifEmpty {
                            "Faktor ini menunjukkan apakah Anda telah didiagnosis menderita tekanan darah tinggi (hipertensi). Memiliki hipertensi sangat erat kaitannya dengan resistensi insulin, yang meningkatkan peluang Anda terkena diabetes."
                        },
                        isCholesterol = prediction.isCholesterol,
                        isCholesterolContribution = isCholesterolContribution * 100,
                        isCholesterolExplanation = prediction.isCholesterolExplanation.ifEmpty {
                            "Faktor ini mencatat apakah Anda memiliki kadar kolesterol tinggi. Kolesterol tinggi sering kali menyertai faktor risiko lain dan dapat berkontribusi pada kondisi yang mengarah ke diabetes tipe 2."
                        },
                        isBloodline = prediction.isBloodline,
                        isBloodlineContribution = isBloodlineContribution * 100,
                        isBloodlineExplanation = prediction.isBloodlineExplanation.ifEmpty {
                            "Faktor ini mencerminkan apakah Anda memiliki riwayat diabetes dalam keluarga langsung (orang tua). Adanya faktor keturunan merupakan salah satu hal yang diketahui dapat meningkatkan risiko Anda."
                        },
                        isMacrosomicBaby = prediction.isMacrosomicBaby,
                        isMacrosomicBabyContribution = isMacrosomicBabyContribution * 100,
                        isMacrosomicBabyExplanation = prediction.isMacrosomicBabyExplanation.ifEmpty {
                            "Faktor ini menunjukkan apakah Anda pernah melahirkan bayi dengan berat di atas 4 kg. Riwayat semacam ini bisa menjadi tanda adanya diabetes gestasional saat kehamilan, yang membuat Anda lebih rentan terkena diabetes tipe 2 di kemudian hari."
                        },
                        smokingStatus = prediction.smokingStatus,
                        smokingStatusContribution = smokingStatusContribution * 100,
                        avgSmokeCount = prediction.avgSmokeCount,
                        smokingStatusExplanation = prediction.smokingStatusExplanation.ifEmpty {
                            "Faktor ini menjelaskan status merokok Anda saat ini, baik perokok aktif, mantan perokok, atau tidak pernah merokok. Merokok dapat meningkatkan peradangan dan resistensi insulin, sehingga menaikkan risiko diabetes Anda secara keseluruhan."
                        },
                        physicalActivityFrequency = prediction.physicalActivityFrequency,
                        physicalActivityFrequencyContribution = physicalActivityContribution * 100,
                        physicalActivityFrequencyExplanation = prediction.physicalActivityFrequencyExplanation.ifEmpty {
                            "Faktor ini mengukur seberapa sering Anda melakukan aktivitas fisik dalam seminggu. Olahraga teratur membantu mengontrol berat badan dan meningkatkan cara tubuh menggunakan insulin, sehingga menurunkan risiko diabetes Anda."
                        },
                        createdAt = prediction.createdAt
                    )
                )
            }

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

    override suspend fun whatIfPrediction(whatIfRequest: WhatIfPredictionRequest): Resource<WhatIfPredictionResponse> {
        return Resource.Error("Use startWhatIfJob instead")
    }

    override suspend fun startWhatIfJob(whatIfRequest: WhatIfPredictionRequest): Resource<WhatIfJobResponse> {
        return try {
            val response = predictionApiService.whatIfPrediction(whatIfRequest)
            Resource.Success(response)
        } catch (e: IOException) {
            Resource.Error("${e.message}")
        } catch (e: HttpException) {
            Resource.Error("${e.message}")
        }
    }

    override suspend fun pollWhatIfJob(jobId: String, pollingIntervalMs: Long): Flow<WhatIfJobStatus> {
        return whatIfJobManager.pollJobStatus(jobId, pollingIntervalMs)
    }

    override suspend fun getWhatIfJobResult(jobId: String): Resource<WhatIfJobResultResponse> {
        return try {
            val response = predictionApiService.getWhatIfJobResult(jobId)
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
}

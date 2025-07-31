package com.itb.diabetify.presentation.guide.guide_detail

import com.itb.diabetify.R

data class GuideDetailData(
    val title: String,
    val content: List<GuideSection>
)

data class GuideSection(
    val title: String,
    val content: String,
    val imageResId: Int? = null,
    val source: GuideSource? = null
)

data class GuideSource(
    val title: String,
    val url: String
)

val guideDetails = mapOf(
    "about_diabetes" to GuideDetailData(
        title = "Tentang Diabetes",
        content = listOf(
            GuideSection(
                title = "Apa itu Diabetes?",
                content = """Diabetes adalah penyakit kronis yang ditandai dengan kadar gula darah (glukosa) yang tinggi akibat gangguan produksi atau kerja insulin. Insulin adalah hormon yang membantu glukosa masuk ke dalam sel untuk digunakan sebagai energi. Jika insulin tidak cukup atau tidak bekerja dengan baik, glukosa menumpuk di dalam darah dan dapat menyebabkan berbagai komplikasi kesehatan.

Diabetes dapat menyebabkan masalah serius seperti penyakit jantung, kerusakan ginjal, gangguan penglihatan, dan gangguan saraf jika tidak dikelola dengan baik.""",
                source = GuideSource(
                    title = "World Health Organization - Diabetes",
                    url = "https://www.who.int/news-room/fact-sheets/detail/diabetes"
                )
            ),
            GuideSection(
                title = "Jenis-jenis Diabetes",
                content = """1. Diabetes Tipe 1
• Disebabkan oleh kerusakan sistem imun yang menyerang sel penghasil insulin di pankreas
• Umumnya muncul pada anak-anak atau dewasa muda
• Penderita memerlukan suntikan insulin setiap hari

2. Diabetes Tipe 2
• Terjadi ketika tubuh tidak menggunakan insulin secara efektif (resistensi insulin) atau produksi insulin menurun
• Merupakan jenis diabetes yang paling banyak ditemukan (sekitar 90% kasus)
• Sering terkait dengan gaya hidup seperti pola makan tidak sehat, kurang aktivitas fisik, dan obesitas
• Dapat dicegah atau dikendalikan dengan perubahan gaya hidup sehat

3. Diabetes Gestasional
• Terjadi pada wanita selama masa kehamilan
• Biasanya hilang setelah melahirkan, namun meningkatkan risiko diabetes tipe 2 di kemudian hari bagi ibu dan anak""",
                source = GuideSource(
                    title = "CDC - Diabetes Basics",
                    url = "https://www.cdc.gov/diabetes/about/?CDC_AAref_Val=https://www.cdc.gov/diabetes/basics/diabetes.html"
                )
            ),
            GuideSection(
                title = "Gejala Umum Diabetes",
                content = """• Sering merasa haus dan sering buang air kecil
• Mudah merasa lapar
• Penurunan berat badan tanpa sebab yang jelas
• Penglihatan kabur
• Luka yang sulit sembuh
• Mudah lelah atau lemas
• Kesemutan atau mati rasa di tangan dan kaki""",
                source = GuideSource(
                    title = "Kementerian Kesehatan - Mengenal Gejala Diabetes Melitus",
                    url = "https://upk.kemkes.go.id/new/mengenal-gejala-diabetes-melitus"
                )
            )
        )
    ),
    "risk_factors" to GuideDetailData(
        title = "Faktor Risiko",
        content = listOf(
            GuideSection(
                title = "Faktor Risiko yang Dapat Dimodifikasi",
                content = """• Status Merokok: Kondisi kebiasaan merokok pengguna, dapat berupa tidak pernah merokok, sudah berhenti merokok, atau masih aktif merokok.

• Indeks Brinkman: Kategori risiko berdasarkan kebiasaan merokok. Dihitung dengan rumus: `rata-rata rokok per hari x lama merokok (tahun)`. Kategori meliputi perokok ringan, sedang, dan berat.

• Indeks Massa Tubuh: Rasio berat badan terhadap tinggi badan untuk menilai status gizi. Dihitung dengan rumus: `berat badan (kg) / (tinggi badan (m)²)`.

• Hipertensi: Status yang menandakan apakah pengguna memiliki tekanan darah tinggi.

• Kolesterol: Status yang menandakan apakah pengguna memiliki kadar kolesterol tinggi.

• Aktivitas Fisik: Jumlah total hari dalam seminggu saat pengguna melakukan aktivitas fisik dengan intensitas sedang.""",
            ),
            GuideSection(
                title = "Faktor Risiko yang Tidak Dapat Dimodifikasi",
                content = """• Usia: Usia pengguna saat ini.

• Riwayat Keluarga: Informasi apakah orang tua kandung pengguna meninggal akibat komplikasi diabetes.

• Riwayat Bayi Makrosomia: Informasi apakah pengguna pernah melahirkan bayi dengan berat badan lahir di atas 4 kg. (Tidak relevan untuk pengguna pria atau yang belum pernah hamil).""",
            )
        )
    ),
    "about_xai" to GuideDetailData(
        title = "Tentang XAI",
        content = listOf(
            GuideSection(
                title = "Apa itu XAI?",
                content = """XAI (Explainable Artificial Intelligence) atau Kecerdasan Buatan yang Dapat Dijelaskan adalah pendekatan dalam pengembangan sistem AI yang tidak hanya memberikan prediksi atau keputusan, tetapi juga dapat menjelaskan alasan di balik keputusan tersebut.
                    
Dalam konteks Diabetify, XAI membantu menjelaskan bagaimana berbagai faktor berkontribusi terhadap risiko diabetes seseorang.""",
                source = GuideSource(
                    title = "Explaining Explanations - An Overview of Interpretability of Machine Learning",
                    url = "https://arxiv.org/pdf/1806.00069"
                )
            ),
            GuideSection(
                title = "Mengapa XAI Penting?",
                content = """1. Transparansi
• Memahami alasan di balik prediksi
• Meningkatkan kepercayaan terhadap sistem

2. Edukasi
• Membantu pengguna memahami faktor risiko mereka
• Memberikan wawasan untuk perbaikan gaya hidup

3. Personalisasi
• Memungkinkan rekomendasi yang lebih tepat sasaran
• Membantu fokus pada faktor yang paling berpengaruh""",
                source = GuideSource(
                    title = "Towards A Rigorous Science of Interpretable Machine Learning",
                    url = "https://arxiv.org/pdf/1702.08608"
                )
            ),
            GuideSection(
                title = "Hasil XAI di Diabetify",
                content = """Gambar berikut menunjukkan bagaimana XAI digunakan di Diabetify untuk menjelaskan kontribusi berbagai faktor risiko terhadap skor risiko diabetes pengguna. Setiap faktor ditampilkan dengan persentase kontribusi dan penjelasan singkat mengenai pengaruhnya terhadap risiko diabetes.""",
                imageResId = R.drawable.food,
            )
        )
    ),
    "ai_prediction" to GuideDetailData(
        title = "Perhitungan AI",
        content = listOf(
            GuideSection(
                title = "Bagaimana AI dan XAI Bekerja di Diabetify",
                content = """Aplikasi ini menggunakan teknologi AI (Artificial Intelligence) untuk membantu memprediksi risiko dini seseorang terkena diabetes berdasarkan data kesehatan dan gaya hidup. Model AI yang digunakan adalah XGBoost, sebuah algoritma pembelajaran mesin yang banyak digunakan secara global untuk prediksi berbasis data numerik dan kategorikal. Model ini dilatih menggunakan data dari survei IFLS5 (Indonesia Family Life Survey ke-5), khusus untuk individu berusia 20 tahun ke atas yang memiliki data pemeriksaan HbA1c, yaitu indikator penting dalam diagnosis diabetes. AI mempelajari pola dari berbagai informasi seperti usia, indeks massa tubuh (BMI), kebiasaan merokok, tekanan darah tinggi, kadar kolesterol, riwayat keluarga, aktivitas fisik, dan riwayat melahirkan bayi besar (macrosomic baby), lalu menghasilkan prediksi kemungkinan seseorang mengidap diabetes."""
            ),
            GuideSection(
                title = "Validitas Hasil Prediksi",
                content = """Untuk mengukur akurasi prediksi, model ini dievaluasi menggunakan metrik ROC AUC dan memperoleh skor antara 0,71 hingga 0,72, yang menunjukkan bahwa model cukup baik dalam membedakan siapa yang berisiko dan siapa yang tidak. Namun tidak berhenti di situ, aplikasi ini juga menggunakan pendekatan XAI (Explainable AI) untuk memastikan prediksi AI bisa dijelaskan secara transparan. Teknologi XAI yang digunakan adalah SHAP (SHapley Additive exPlanations), yang memungkinkan pengguna melihat kontribusi masing-masing faktor terhadap hasil prediksi mereka. Misalnya, SHAP bisa menunjukkan bahwa riwayat keluarga atau BMI Anda merupakan faktor dominan dalam hasil perhitungan risiko.

Pendekatan ini tidak hanya memberikan angka probabilitas, tetapi juga penjelasan di baliknya, sehingga pengguna bisa memahami mengapa mereka dikategorikan berisiko atau tidak. Penjelasan ini telah ditinjau oleh tenaga ahli di bidang kesehatan, sehingga tetap selaras dengan pemahaman medis."""
            ),
            GuideSection(
                title = "Menggunakan Hasil Prediksi",
                content = """1. Pahami Faktor Risiko Anda
• Identifikasi faktor dengan kontribusi tertinggi
• Fokus pada faktor yang dapat dimodifikasi

2. Ambil Tindakan
• Tetapkan target perbaikan yang realistis
• Pantau perubahan secara berkala

3. Konsultasi dengan Profesional
• Bagikan hasil prediksi dengan dokter
• Dapatkan panduan medis yang sesuai
• Buat rencana pencegahan personal"""
            )
        )
    )
) 
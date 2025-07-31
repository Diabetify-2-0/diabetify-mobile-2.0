package com.itb.diabetify.presentation.guide

data class FAQCardData(
    val question: String = "",
    val answer: String = ""
)

val faqCards = listOf(
    FAQCardData(
        question = "Seberapa akurat estimasi risiko diabetes dari aplikasi ini?",
        answer = "Estimasi risiko dalam aplikasi ini dibuat menggunakan model AI berbasis XGBoost yang telah dilatih dengan data survei IFLS5 dan divalidasi oleh tenaga ahli. Model ini memiliki tingkat akurasi yang cukup baik, dengan skor evaluasi ROC AUC sebesar 0,71–0,72. Artinya, model cukup andal dalam membedakan siapa yang memiliki risiko diabetes dan siapa yang tidak, terutama dalam konteks populasi umum di Indonesia."
    ),
    FAQCardData(
        question = "Apakah hasil estimasi risiko bisa menggantikan konsultasi dokter?",
        answer = "Tidak. Hasil estimasi risiko Diabetify adalah alat bantu untuk mengetahui risiko diabetes, bukan pengganti konsultasi medis profesional. Aplikasi ini membantu Anda memahami faktor risiko dan memberikan rekomendasi gaya hidup sehat. Jika hasil menunjukkan risiko tinggi atau Anda memiliki gejala diabetes, sangat disarankan untuk segera berkonsultasi dengan dokter untuk pemeriksaan dan diagnosis yang tepat."
    ),
    FAQCardData(
        question = "Apakah data pribadi saya aman di aplikasi ini?",
        answer = "Ya, keamanan data Anda adalah prioritas utama. Diabetify menggunakan enkripsi end-to-end untuk melindungi semua informasi kesehatan Anda. Data disimpan secara aman di server yang memenuhi standar keamanan medis. Kami tidak membagikan data pribadi Anda kepada pihak ketiga tanpa persetujuan."
    ),
    FAQCardData(
        question = "Seberapa sering saya harus melakukan input data kesehatan saya?",
        answer = "Disarankan untuk melakukan input data kesehatan harian (aktivitas merokok dan aktivitas fisik) setiap hari, sedangkan untuk data kesehatan non-harian diubah ketika ada perubahan signifikan pada kondisi kesehatan Anda, seperti perubahan berat badan, tekanan darah, dan lainnya. Ini membantu memantau perkembangan risiko diabetes Anda dari waktu ke waktu. Aplikasi juga akan mengingatkan Anda untuk update data secara berkala."
    ),
    FAQCardData(
        question = "Apa yang harus dilakukan jika hasil prediksi menunjukkan risiko tinggi?",
        answer = "Jika hasil menunjukkan risiko tinggi, jangan panik. Segera konsultasi dengan dokter untuk pemeriksaan lebih lanjut seperti tes gula darah puasa atau HbA1c. Gunakan fitur tips dan panduan dalam aplikasi untuk memulai gaya hidup sehat. Ingat, deteksi dini memungkinkan pencegahan yang lebih efektif."
    ),
    FAQCardData(
        question = "Apakah aplikasi ini cocok untuk semua usia?",
        answer = "Aplikasi ini dirancang khusus untuk individu berusia 20 tahun ke atas, karena model AI-nya difokuskan pada prediksi diabetes tipe 2, yang umumnya berkembang pada usia dewasa. Model ini dilatih menggunakan data dari survei IFLS5 untuk kelompok usia tersebut. Oleh karena itu, bagi pengguna di bawah usia 20 tahun yang lebih berisiko mengalami diabetes tipe 1 hasil prediksi mungkin kurang relevan atau akurat, karena berada di luar cakupan data dan tujuan model."
    ),
)
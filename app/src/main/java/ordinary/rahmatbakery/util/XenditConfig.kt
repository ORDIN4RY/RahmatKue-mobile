package ordinary.rahmatbakery.util

object XenditConfig {
    // Ganti dengan Secret Key dari Xendit Sandbox Anda
    // Dashboard: https://dashboard.xendit.co/settings/developers#api-keys
    const val XENDIT_SECRET_KEY = "xnd_development_L8a0XC9R7LrhJC4szaFlEWjuqvsvwpH7ftz6nafWsLwUVOfVZ2QKFfWvwOgI"

    const val BASE_URL = "https://api.xendit.co"

    // Jenis pembayaran yang tersedia di sandbox
    enum class PaymentType {
        QRIS,
    }
}
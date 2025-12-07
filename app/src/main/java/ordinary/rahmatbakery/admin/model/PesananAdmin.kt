package ordinary.rahmatbakery.admin.model

/**
 * Model untuk Pesanan di Admin Dashboard
 */
data class PesananAdmin(
    val id: Int,
    val tanggal: String,
    val waktu: String,
    val nomor: String,
    val jenis: String,
    val status: String
)

/**
 * Model untuk Produk
 */
data class Produk(
    val id: String,
    val nama: String,
    val gambarUrl: String?,
    val jumlahTerjual: Int
)

/**
 * Model untuk Activity Log
 */
data class ActivityLog(
    val id: String,
    val activity: String,
    val description: String,
    val timestamp: String,
    val user: String,
    val type: ActivityType
)

/**
 * Tipe Activity
 */
enum class ActivityType {
    ORDER_CREATED,      // 📦 Order baru dibuat
    ORDER_CONFIRMED,    // ✅ Order dikonfirmasi
    ORDER_COMPLETED,    // 🎉 Order selesai
    ORDER_CANCELLED,    // ❌ Order dibatalkan
    PAYMENT_RECEIVED,   // 💰 Pembayaran diterima
    PROMO_CREATED,      // 🎁 Promo dibuat
    PROMO_ACTIVATED,    // 🎉 Promo diaktifkan
    PRODUCT_ADDED,      // ➕ Produk ditambah
    PRODUCT_UPDATED,    // ✏️ Produk diupdate
    USER_REGISTERED     // 👤 User baru daftar
}
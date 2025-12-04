package ordinary.rahmatbakery.admin.activity

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import ordinary.rahmatbakery.model.Cancellation
import ordinary.rahmatbakery.model.OrderAdmin
import io.github.jan.supabase.postgrest.query.Order
import ordinary.rahmatbakery.api.SupabaseManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
class OrderRepository {

    private val supabase = SupabaseManager.client

    /**
     * Get all orders dengan detail lengkap
     */
    suspend fun getAllOrders(): List<OrderAdmin> {
        return try {
            supabase.from("transaksi")
                .select(
                    Columns.raw("""
                        *,
                        profiles:id_user(*),
                        alamat:id_alamat(*),
                        pembayaran(*),
                        detail_transaksi_produk(*, produk:id_produk(*)),
                        detail_transaksi_paket(*, paket:id_paket(*)),
                        batal(*)
                    """.trimIndent())
                ) {
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<OrderAdmin>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get order by ID dengan detail lengkap
     */
    suspend fun getOrderById(orderId: String): OrderAdmin? {
        return try {
            supabase.from("transaksi")
                .select(
                    Columns.raw("""
                        *,
                        profiles:id_user(*),
                        alamat:id_alamat(*),
                        pembayaran(*),
                        detail_transaksi_produk(*, produk:id_produk(*)),
                        detail_transaksi_paket(*, paket:id_paket(*)),
                        batal(*)
                    """.trimIndent())
                ) {
                    filter {
                        eq("id_transaksi", orderId)
                    }
                }
                .decodeSingle<OrderAdmin>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Update status pesanan
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Boolean {
        return try {
            supabase.from("transaksi")
                .update({
                    set("status", status)
                }) {
                    filter {
                        eq("id_transaksi", orderId)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }



    /**
     * Batalkan pesanan dengan alasan
     */
    suspend fun cancelOrder(orderId: String, alasan: String, adminId: String): Boolean {
        return try {

            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(Date())
            // Insert data pembatalan
            supabase.from("batal")
                .insert(
                    mapOf(
                        "id_transaksi" to orderId,
                        "alasan" to alasan,
                        "tipe" to "admin",
                        "status" to "Dikonfirmasi",
                        "dikonfirmasi_oleh" to adminId,
                        "dikonfirmasi_pada" to nowIso
                    )
                )

            // Update status transaksi
            updateOrderStatus(orderId, "Dibatalkan")

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Selesaikan pesanan
     */
    suspend fun completeOrder(orderId: String): Boolean {
        return updateOrderStatus(orderId, "Selesai")
    }

    /**
     * Proses pesanan (dari Menunggu Pembayaran ke Sedang Diproses)
     */
    suspend fun processOrder(orderId: String): Boolean {
        return updateOrderStatus(orderId, "Sedang Diproses")
    }

    /**
     * Get orders by status
     */
    suspend fun getOrdersByStatus(status: String): List<Order> {
        return try {
            supabase.from("transaksi")
                .select(
                    Columns.raw("""
                        *,
                        profiles:id_user(*),
                        alamat:id_alamat(*),
                        pembayaran(*),
                        detail_transaksi_produk(*, produk:id_produk(*)),
                        detail_transaksi_paket(*, paket:id_paket(*)),
                        batal(*)
                    """.trimIndent())
                ) {
                    filter {
                        eq("status", status)
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Order>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
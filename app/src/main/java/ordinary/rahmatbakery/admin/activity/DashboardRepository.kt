package ordinary.rahmatbakery.admin.activity

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ordinary.rahmatbakery.admin.model.ActivityLog
import ordinary.rahmatbakery.admin.model.ActivityType
import ordinary.rahmatbakery.util.SupabaseManager

object DashboardRepository {

    // Initialize Supabase Client
    val client= SupabaseManager.client

    // ==================== DATA MODELS ====================

    @Serializable
    data class TransaksiWithPembayaran(
        @SerialName("id_transaksi") val idTransaksi: String,
        @SerialName("nomor_pesanan") val nomorPesanan: String?,
        @SerialName("created_at") val createdAt: String,
        @SerialName("total_harga") val totalHarga: Int,
        @SerialName("dp_minimal") val dpMinimal: Int?,
        val status: String?,
        @SerialName("metode_pengambilan") val metodePengambilan: String?,
        val pembayaran: List<Pembayaran>? = null
    )

    @Serializable
    data class Pembayaran(
        @SerialName("id_pembayaran") val idPembayaran: String,
        val nominal: Int?,
        val metode: String?,
        val status: String?
    )

    @Serializable
    data class TransaksiStatus(
        val status: String?
    )

    @Serializable
    data class ProdukWithSales(
        @SerialName("id_produk") val idProduk: String,
        @SerialName("nama_produk") val namaProduk: String,
        @SerialName("foto_produk") val fotoProduk: String?,
        val harga: Int
    )

    @Serializable
    data class DetailProdukSales(
        @SerialName("id_produk") val idProduk: String?,
        val jumlah: Int?
    )

    // ==================== QUERY FUNCTIONS ====================

    /**
     * Mendapatkan statistik pembayaran (Tunai vs Piutang)
     * @param bulan Filter bulan (1-12), null untuk semua bulan
     * @param tahun Filter tahun
     */
    suspend fun getPaymentStatistics(bulan: Int? = null, tahun: Int): PaymentStats {
        try {
            // Ambil semua transaksi dengan pembayaran
            val query = """
                id_transaksi,
                total_harga,
                dp_minimal,
                created_at,
                pembayaran(
                    nominal,
                    metode,
                    status
                )
            """.trimIndent()

            val result = client.from("transaksi")
                .select(Columns.raw(query)) {
                    // Filter berdasarkan tahun
                    filter {
                        gte("created_at", "$tahun-01-01T00:00:00")
                        lte("created_at", "$tahun-12-31T23:59:59")

                        // Filter berdasarkan bulan jika ada
                        if (bulan != null) {
                            val bulanStr = bulan.toString().padStart(2, '0')
                            gte("created_at", "$tahun-$bulanStr-01T00:00:00")

                            // Hitung hari terakhir bulan
                            val lastDay = when (bulan) {
                                2 -> if (tahun % 4 == 0) 29 else 28
                                4, 6, 9, 11 -> 30
                                else -> 31
                            }
                            lte("created_at", "$tahun-$bulanStr-${lastDay}T23:59:59")
                        }
                    }
                }
                .decodeList<TransaksiWithPembayaran>()

            var tunaiCount = 0
            var totalPiutang = 0

            result.forEach { transaksi ->
                val totalPembayaran = transaksi.pembayaran?.sumOf { it.nominal ?: 0 } ?: 0
                val sisaPembayaran = transaksi.totalHarga - totalPembayaran

                // Jika sudah lunas, hitung sebagai tunai
                if (sisaPembayaran <= 0) {
                    tunaiCount++
                } else {
                    // Jika masih ada sisa, masuk piutang
                    totalPiutang += sisaPembayaran
                }
            }

            return PaymentStats(tunaiCount, totalPiutang)
        } catch (e: Exception) {
            e.printStackTrace()
            return PaymentStats(0, 0)
        }
    }

    /**
     * Mendapatkan statistik order berdasarkan status
     * @param bulan Filter bulan (1-12), null untuk semua bulan
     * @param tahun Filter tahun
     */
    suspend fun getOrderStatistics(bulan: Int? = null, tahun: Int): OrderStats {
        try {
            val result = client.from("transaksi")
                .select(Columns.list("status", "created_at")) {
                    filter {
                        gte("created_at", "$tahun-01-01T00:00:00")
                        lte("created_at", "$tahun-12-31T23:59:59")

                        if (bulan != null) {
                            val bulanStr = bulan.toString().padStart(2, '0')
                            gte("created_at", "$tahun-$bulanStr-01T00:00:00")

                            val lastDay = when (bulan) {
                                2 -> if (tahun % 4 == 0) 29 else 28
                                4, 6, 9, 11 -> 30
                                else -> 31
                            }
                            lte("created_at", "$tahun-$bulanStr-${lastDay}T23:59:59")
                        }
                    }
                }
                .decodeList<TransaksiStatus>()

            var selesai = 0
            var proses = 0
            var dibatalkan = 0

            result.forEach { transaksi ->
                when (transaksi.status?.lowercase()) {
                    "selesai", "pesanan selesai" -> selesai++
                    "diproses", "sedang diproses", "menunggu pembayaran",
                    "menunggu konfirmasi" -> proses++
                    "dibatalkan", "batal" -> dibatalkan++
                }
            }

            return OrderStats(selesai, proses, dibatalkan)
        } catch (e: Exception) {
            e.printStackTrace()
            return OrderStats(0, 0, 0)
        }
    }

    /**
     * Mendapatkan 10 pesanan terbaru
     * @param bulan Filter bulan (1-12), null untuk semua bulan
     * @param tahun Filter tahun
     */
    suspend fun getRecentOrders(bulan: Int? = null, tahun: Int): List<TransaksiWithPembayaran> {
        return try {
            val query = """
                id_transaksi,
                nomor_pesanan,
                created_at,
                total_harga,
                status,
                metode_pengambilan
            """.trimIndent()

            client.from("transaksi")
                .select(Columns.raw(query)) {
                    filter {
                        gte("created_at", "$tahun-01-01T00:00:00")
                        lte("created_at", "$tahun-12-31T23:59:59")

                        if (bulan != null) {
                            val bulanStr = bulan.toString().padStart(2, '0')
                            gte("created_at", "$tahun-$bulanStr-01T00:00:00")

                            val lastDay = when (bulan) {
                                2 -> if (tahun % 4 == 0) 29 else 28
                                4, 6, 9, 11 -> 30
                                else -> 31
                            }
                            lte("created_at", "$tahun-$bulanStr-${lastDay}T23:59:59")
                        }
                    }
                    order("created_at", Order.DESCENDING)
                    limit(10)
                }
                .decodeList<TransaksiWithPembayaran>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Mendapatkan produk terlaris berdasarkan jumlah penjualan
     * @param bulan Filter bulan (1-12), null untuk semua bulan
     * @param tahun Filter tahun
     */
    suspend fun getTopSellingProducts(bulan: Int? = null, tahun: Int): List<ProductWithSales> {
        return try {
            // Step 1: Get transaksi IDs yang sesuai filter
            val transaksiIds = client.from("transaksi")
                .select(Columns.list("id_transaksi")) {
                    filter {
                        gte("created_at", "$tahun-01-01T00:00:00")
                        lte("created_at", "$tahun-12-31T23:59:59")

                        if (bulan != null) {
                            val bulanStr = bulan.toString().padStart(2, '0')
                            gte("created_at", "$tahun-$bulanStr-01T00:00:00")

                            val lastDay = when (bulan) {
                                2 -> if (tahun % 4 == 0) 29 else 28
                                4, 6, 9, 11 -> 30
                                else -> 31
                            }
                            lte("created_at", "$tahun-$bulanStr-${lastDay}T23:59:59")
                        }
                    }
                }

            // Step 2: Ambil detail produk dari transaksi tersebut
            val salesData = client.from("detail_transaksi_produk")
                .select(Columns.list("id_produk", "jumlah"))
                .decodeList<DetailProdukSales>()

            // Step 3: Group by id_produk dan sum jumlah
            val productSalesMap = mutableMapOf<String, Int>()
            salesData.forEach { detail ->
                detail.idProduk?.let { id ->
                    productSalesMap[id] = (productSalesMap[id] ?: 0) + (detail.jumlah ?: 0)
                }
            }

            // Step 4: Sort by jumlah dan ambil top 5
            val topProductIds = productSalesMap.entries
                .sortedByDescending { it.value }
                .take(5)

            // Step 5: Ambil detail produk
            val products = mutableListOf<ProductWithSales>()
            topProductIds.forEach { (productId, totalSales) ->
                try {
                    val produk = client.from("produk")
                        .select(Columns.list(
                            "id_produk",
                            "nama_produk",
                            "foto_produk",
                            "harga"
                        )) {
                            filter {
                                eq("id_produk", productId)
                            }
                        }
                        .decodeSingle<ProdukWithSales>()

                    products.add(
                        ProductWithSales(
                            idProduk = produk.idProduk,
                            namaProduk = produk.namaProduk,
                            fotoProduk = produk.fotoProduk,
                            harga = produk.harga,
                            totalTerjual = totalSales
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            products
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Mendapatkan jumlah promo aktif
     */
    suspend fun getPromoAktifCount(): Int {
        return try {
            @Serializable
            data class PromoCount(
                @SerialName("id_promo") val idPromo: String
            )

            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())

            val result = client.from("promo")
                .select(Columns.list("id_promo")) {
                    filter {
                        lte("tgl_mulai", today)
                        gte("tgl_berakhir", today)
                    }
                }
                .decodeList<PromoCount>()

            result.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Mendapatkan jumlah order menunggu konfirmasi
     */
    suspend fun getMenungguKonfirmasiCount(bulan: Int? = null, tahun: Int): Int {
        return try {
            val result = client.from("transaksi")
                .select(Columns.list("status", "created_at")) {
                    filter {
                        eq("status", "Menunggu Diproses")

                        gte("created_at", "$tahun-01-01T00:00:00")
                        lte("created_at", "$tahun-12-31T23:59:59")

                        if (bulan != null) {
                            val bulanStr = bulan.toString().padStart(2, '0')
                            gte("created_at", "$tahun-$bulanStr-01T00:00:00")

                            val lastDay = when (bulan) {
                                2 -> if (tahun % 4 == 0) 29 else 28
                                4, 6, 9, 11 -> 30
                                else -> 31
                            }
                            lte("created_at", "$tahun-$bulanStr-${lastDay}T23:59:59")
                        }
                    }
                }

            result.data.toString().split(",").size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Mendapatkan activity log count
     */
    suspend fun getActivityLogCount(bulan: Int? = null, tahun: Int): Int {
        return try {
            // Hitung dari transaksi + pembayaran + batal
            val transaksiCount = client.from("transaksi")
                .select(Columns.list("id_transaksi")) {
                    filter {
                        gte("created_at", "$tahun-01-01T00:00:00")
                        lte("created_at", "$tahun-12-31T23:59:59")

                        if (bulan != null) {
                            val bulanStr = bulan.toString().padStart(2, '0')
                            gte("created_at", "$tahun-$bulanStr-01T00:00:00")

                            val lastDay = when (bulan) {
                                2 -> if (tahun % 4 == 0) 29 else 28
                                4, 6, 9, 11 -> 30
                                else -> 31
                            }
                            lte("created_at", "$tahun-$bulanStr-${lastDay}T23:59:59")
                        }
                    }
                }

            transaksiCount.data.toString().split(",").size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Mendapatkan activity log dengan detail
     */


    suspend fun getActivityLog(
        bulan: Int? = null,
        tahun: Int,
        limit: Int = 25
    ): List<ActivityLog> {

        val list = mutableListOf<ActivityLog>()

        fun dateFilterStart(): String {
            val m = bulan?.toString()?.padStart(2, '0') ?: "01"
            return "$tahun-$m-01T00:00:00"
        }

        fun dateFilterEnd(): String {
            val lastDay = if (bulan == null) 31 else when (bulan) {
                2 -> if (tahun % 4 == 0) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            val m = bulan?.toString()?.padStart(2, '0') ?: "12"
            return "$tahun-$m-$lastDay" + "T23:59:59"

        }

        val start = dateFilterStart()
        val end = dateFilterEnd()

        // ================================
        // 1. TRANSAKSI
        // ================================
        try {
            @Serializable
            data class T(
                val id_transaksi: String,
                val nomor_pesanan: String?,
                val created_at: String,
                val status: String
            )

            val trx = client.from("transaksi")
                .select(
                    Columns.list(
                        "id_transaksi",
                        "nomor_pesanan",
                        "created_at",
                        "status"
                    )
                )
                {
                    filter {
                        gte("created_at", start)
                        lte("created_at", end)
                    }
                }
                .decodeList<T>()

            trx.forEach { t ->
                val type = when (t.status.lowercase()) {
                    "menunggu pembayaran" -> ActivityType.ORDER_CREATED
                    "menunggu konfirmasi" -> ActivityType.ORDER_CONFIRMED
                    "selesai" -> ActivityType.ORDER_COMPLETED
                    "dibatalkan" -> ActivityType.ORDER_CANCELLED
                    else -> ActivityType.ORDER_CONFIRMED
                }

                list += ActivityLog(
                    id = t.id_transaksi,
                    activity = "Order ${t.status}",
                    description = "Nomor pesanan: ${t.nomor_pesanan ?: "-"}",
                    timestamp = t.created_at,
                    user = "Sistem",
                    type = type
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ================================
        // 2. PEMBAYARAN
        // ================================
        try {
            @Serializable
            data class Pay(
                val id_pembayaran: String,
                val nominal: Int?,
                val metode: String?,
                val status: String?,
                val tgl_pembayaran: String
            )

            val pembayaran = client.from("pembayaran")
                .select(
                    Columns.list(
                        "id_pembayaran",
                        "nominal",
                        "metode",
                        "status",
                        "tgl_pembayaran"
                    )
                )
                {
                    filter {
                        gte("tgl_pembayaran", start)
                        lte("tgl_pembayaran", end)
                    }
                }
                .decodeList<Pay>()

            pembayaran.forEach { p ->
                list += ActivityLog(
                    id = p.id_pembayaran,
                    activity = "Pembayaran ${p.status ?: "diproses"}",
                    description = "${p.metode} - Rp${p.nominal}",
                    timestamp = p.tgl_pembayaran,
                    user = "Sistem",
                    type = ActivityType.PAYMENT_RECEIVED
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        // ================================
        // 3. PEMBATALAN
        // ================================
        try {
            @Serializable
            data class BatalAct(
                val id_batal: String,
                val alasan: String?,
                val dibuat_pada: String,
                val status: String?
            )

            val pembatalan = client.from("batal")
                .select(Columns.list(
                    "id_batal, " +
                            "alasan, " +
                            "dibuat_pada, " +
                            "status")) {
                    filter {
                        gte("dibuat_pada", start)
                        lte("dibuat_pada", end)
                    }
                }
                .decodeList<BatalAct>()

            pembatalan.forEach { b ->
                list += ActivityLog(
                    id = b.id_batal,
                    activity = "Pengajuan Pembatalan",
                    description = "Alasan: ${b.alasan}",
                    timestamp = b.dibuat_pada,
                    user = "Pelanggan",
                    type = ActivityType.ORDER_CANCELLED
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        // ================================
        // 4. PRODUK
        // ================================
        try {
            @Serializable
            data class Prod(
                val id_produk: String,
                val nama_produk: String,
                val created_at: String,
                val updated_at: String?
            )

            val produk = client.from("produk")
                .select(
                    Columns.list(
                        "id_produk",
                        "nama_produk",
                        "created_at",
                        "updated_at"
                    )
                )
                {
                    filter {
                        gte("created_at", start)
                        lte("created_at", end)
                    }
                }
                .decodeList<Prod>()

            produk.forEach { p ->
                list += ActivityLog(
                    id = p.id_produk,
                    activity = "Produk Ditambahkan",
                    description = p.nama_produk,
                    timestamp = p.created_at,
                    user = "Admin",
                    type = ActivityType.PRODUCT_ADDED
                )
            }

            val produkUpdated = client.from("produk")
                .select(Columns.list(
                    "id_produk, " +
                            "nama_produk, " +
                            "updated_at")) {
                    filter {
                        gte("updated_at", start)
                        lte("updated_at", end)
                    }
                }
                .decodeList<Prod>()

            produkUpdated.forEach { p ->
                list += ActivityLog(
                    id = p.id_produk,
                    activity = "Produk Diperbarui",
                    description = p.nama_produk,
                    timestamp = p.updated_at ?: p.created_at,
                    user = "Admin",
                    type = ActivityType.PRODUCT_UPDATED
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


        // ================================
        // 5. PROMO
        // ================================
        try {
            @Serializable
            data class Promo(
                val id_promo: String,
                val nama_promo: String,
                val created_at: String,
                val aktif: Boolean
            )

            val promoList = client.from("promo")
                .select(Columns.list(
                    "id_promo, " +
                            "nama_promo, " +
                            "created_at, " +
                            "aktif")) {
                    filter {
                        gte("created_at", start)
                        lte("created_at", end)
                    }
                }
                .decodeList<Promo>()

            promoList.forEach { pr ->
                list += ActivityLog(
                    id = pr.id_promo,
                    activity = "Promo Dibuat",
                    description = pr.nama_promo,
                    timestamp = pr.created_at,
                    user = "Admin",
                    type = ActivityType.PROMO_CREATED
                )

                if (pr.aktif) {
                    list += ActivityLog(
                        id = pr.id_promo + "-active",
                        activity = "Promo Diaktifkan",
                        description = pr.nama_promo,
                        timestamp = pr.created_at,
                        user = "Admin",
                        type = ActivityType.PROMO_ACTIVATED
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        // ================================
        // 6. USER TERDAFTAR
        // ================================
        try {
            @Serializable
            data class UserP(
                val id: String,
                val full_name: String?,
                val created_at: String
            )

            val users = client.from("profiles")
                .select(
                    Columns.list(
                        "id",
                        "full_name",
                        "created_at"
                    )
                )
                {
                    filter {
                        gte("created_at", start)
                        lte("created_at", end)
                    }
                }
                .decodeList<UserP>()

            users.forEach { u ->
                list += ActivityLog(
                    id = u.id,
                    activity = "User Baru Terdaftar",
                    description = u.full_name ?: "Pengguna",
                    timestamp = u.created_at,
                    user = u.full_name ?: "User",
                    type = ActivityType.USER_REGISTERED
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // FINAL
        return list.sortedByDescending { it.timestamp }.take(limit)
    }

// ==================== RESPONSE MODELS ====================

    data class PaymentStats(
        val tunaiCount: Int,
        val totalPiutang: Int
    )

    data class OrderStats(
        val selesai: Int,
        val proses: Int,
        val dibatalkan: Int
    )

    data class ProductWithSales(
        val idProduk: String,
        val namaProduk: String,
        val fotoProduk: String?,
        val harga: Int,
        val totalTerjual: Int
    )
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension function untuk format Rupiah
 */
fun Int.toRupiah(): String {
    val localeID = java.util.Locale("in", "ID")
    val formatter = java.text.NumberFormat.getCurrencyInstance(localeID)
    return formatter.format(this).replace("Rp", "Rp ")
}

/**
 * Extension function untuk format tanggal
 */
fun String.toFormattedDate(): Pair<String, String> {
    return try {
        val inputFormat = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            java.util.Locale.getDefault()
        )
        val date = inputFormat.parse(this.replace("Z", "").replace("+00", ""))

        val dateFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())

        Pair(
            date?.let { dateFormat.format(it) } ?: "N/A",
            date?.let { timeFormat.format(it) } ?: "N/A"
        )
    } catch (e: Exception) {
        e.printStackTrace()
        Pair("N/A", "N/A")
    }
}
//package ordinary.rahmatbakery.admin.activity
//
//import ordinary.rahmatbakery.model.DashboardStats
//import ordinary.rahmatbakery.model.TransaksiResponse
//import ordinary.rahmatbakery.model.ProdukTerlaris
//import ordinary.rahmatbakery.model.PesananTerakhir
//import io.github.jan.supabase.postgrest.from
//import io.github.jan.supabase.postgrest.query.Columns
//import io.github.jan.supabase.postgrest.query.Order
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.json.JsonObject
//import ordinary.rahmatbakery.api.SupabaseManager
//
//class DashboardRepository {
//
//
//    // Mendapatkan statistik dashboard
//    suspend fun getDashboardStats(): DashboardStats = withContext(Dispatchers.IO) {
//        val transaksiList = SupabaseManager.client.from("transaksi")
//            .select()
//            .decodeList<TransaksiResponse>()
//
//        val totalPesanan = transaksiList.size
//        val totalPemasukan = transaksiList.sumOf { it.total_harga }
//        val selesai = transaksiList.count { it.status == "Selesai" }
//        val proses = transaksiList.count {
//            it.status == "Menunggu Pembayaran" ||
//                    it.status == "Diproses" ||
//                    it.status == "Siap Diambil"
//        }
//        val batal = transaksiList.count { it.status == "Dibatalkan" }
//
//        DashboardStats(
//            totalPesanan = totalPesanan,
//            totalPemasukan = totalPemasukan,
//            selesai = selesai,
//            proses = proses,
//            batal = batal
//        )
//    }
//
//    // Mendapatkan pesanan terakhir (5 pesanan terbaru)
//    suspend fun getPesananTerakhir(): List<PesananTerakhir> = withContext(Dispatchers.IO) {
//        val response = SupabaseManager.client.from("transaksi")
//            .select(columns = Columns.raw("*, profiles(username)")) {
//                order("created_at", Order.ASCENDING)
//                limit(5)
//            }
//            .decodeList<TransaksiResponse>()
//
//        response.map { transaksi ->
//            // Hitung jumlah item dari detail_transaksi_produk dan detail_transaksi_paket
//            val jumlahProduk = try {
//                SupabaseManager.client.from("detail_transaksi_produk")
//                    .select {
//                        filter {
//                            eq("id_transaksi", transaksi.id_transaksi)
//                        }
//                    }
//                    .decodeList<JsonObject>()
//                    .sumOf { (it["jumlah"] as? Number)?.toInt() ?: 0 }
//            } catch (e: Exception) { 0 }
//
//            val jumlahPaket = try {
//                SupabaseManager.client.from("detail_transaksi_paket")
//                    .select {
//                        filter {
//                            eq("id_transaksi", transaksi.id_transaksi)
//                        }
//                    }
//                    .decodeList<JsonObject>()
//                    .sumOf { (it["jumlah"] as? Number)?.toInt() ?: 0 }
//            } catch (e: Exception) { 0 }
//
//            PesananTerakhir(
//                id = transaksi.id_transaksi.take(8),
//                tglPesan = formatTanggal(transaksi.created_at),
//                tglSelesai = transaksi.waktu_selesai?.let { formatTanggal(it) },
//                nama = transaksi.profiles?.username ?: "Unknown",
//                jumlahItem = jumlahProduk + jumlahPaket,
//                totalHarga = transaksi.total_harga
//            )
//        }
//    }
//
//    // Mendapatkan produk terlaris (top 5)
//    suspend fun getProdukTerlaris(): List<ProdukTerlaris> = withContext(Dispatchers.IO) {
//        // Query untuk menghitung total penjualan per produk
//        val response = SupabaseManager.client.from("detail_transaksi_produk")
//            .select(columns = Columns.raw("id_produk, jumlah, produk(nama_produk, foto_produk)"))
//            .decodeList<JsonObject>()
//
//        // Group by produk dan sum jumlah
//        val produkMap = mutableMapOf<String, ProdukTerlaris>()
//
//        response.forEach { detail ->
//            val idProduk = detail["id_produk"] as? String ?: return@forEach
//            val jumlah = (detail["jumlah"] as? Number)?.toInt() ?: 0
//            val produkData = detail["produk"] as? Map<*, *>
//
//            val namaProduk = produkData?.get("nama_produk") as? String ?: "Unknown"
//            val fotoProduk = produkData?.get("foto_produk") as? String ?: ""
//
//            if (produkMap.containsKey(idProduk)) {
//                val existing = produkMap[idProduk]!!
//                produkMap[idProduk] = existing.copy(
//                    jumlahTerjual = existing.jumlahTerjual + jumlah
//                )
//            } else {
//                produkMap[idProduk] = ProdukTerlaris(
//                    idProduk = idProduk,
//                    namaProduk = namaProduk,
//                    fotoProduk = fotoProduk,
//                    jumlahTerjual = jumlah
//                )
//            }
//        }
//
//        // Sort dan ambil top 5
//        produkMap.values
//            .sortedByDescending { it.jumlahTerjual }
//            .take(5)
//    }
//
//    // Helper function untuk format tanggal
//    private fun formatTanggal(dateString: String): String {
//        return try {
//            // Format: "2024-12-04" -> "04/12"
//            val parts = dateString.split("T")[0].split("-")
//            "${parts[2]}/${parts[1]}"
//        } catch (e: Exception) {
//            dateString
//        }
//    }
//}
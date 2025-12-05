package ordinary.rahmatbakery.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStats(
    val totalPesanan: Int = 0,
    val totalPemasukan: Int = 0,
    val selesai: Int = 0,
    val proses: Int = 0,
    val batal: Int = 0
)

@Serializable
data class PesananTerakhir(
    val id: String,
    val tglPesan: String,
    val tglSelesai: String?,
    val nama: String,
    val jumlahItem: Int,
    val totalHarga: Int
)

@Serializable
data class ProdukTerlaris(
    val idProduk: String,
    val namaProduk: String,
    val fotoProduk: String,
    val jumlahTerjual: Int,
    val produkTerlarisResponse: ProdukTerlarisResponse?=null
)


@Serializable
data class TransaksiResponse(
    val id_transaksi: String,
    val created_at: String,
    val waktu_selesai: String?,
    val total_harga: Int,
    val status: String,
    val profiles: ProfilesResponse?=null
)
@Serializable
data class ProfilesResponse(
    val username: String
)

@Serializable
data class ProdukTerlarisResponse(
    val id_produk: String,
    val nama_produk: String,
    val foto_produk: String,
    val total_terjual: Int
)
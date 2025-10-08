package ordinary.rahmatbakery.pelanggan.model

data class Pesanan(

    val idPesanan: Int,
    val namaPesanan: String,
    val pesananImg : String,
    val status : String,
    val detailPesanan : List<DetailPesanan>,
    val tanggalPesanan : String,
    val totalHarga : String

)

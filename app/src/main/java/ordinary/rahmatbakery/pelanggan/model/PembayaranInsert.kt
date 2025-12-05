package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PembayaranInsert(
    @SerialName("id_transaksi")
    val idTransaksi: String,

    val nominal: Int, // Change to Long if your DB column is BigInt
    val metode: String,
    val status: String,

    @SerialName("invoice_url")
    val invoiceUrl: String
)
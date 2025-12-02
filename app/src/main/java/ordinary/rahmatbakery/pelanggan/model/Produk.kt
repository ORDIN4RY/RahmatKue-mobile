package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import ordinary.rahmatbakery.pelanggan.model.Kategori


@Parcelize
@Serializable
data class Produk (
    val id: String,
    val gambar: String? = null,
    val nama: String,
    val varian: String,
    val kategori: Kategori,
    val deskripsi: String,
    val harga: Int,
    val diskon: Int?= 0,
    val tipe_diskon: String? = null,
    val created_at: String? = null

): Parcelable
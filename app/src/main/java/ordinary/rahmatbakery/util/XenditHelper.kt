package ordinary.rahmatbakery.pelanggan.helper

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ordinary.rahmatbakery.util.XenditConfig
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object XenditHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Membuat Invoice Xendit untuk Sandbox
     * @param externalId ID transaksi dari sistem Anda
     * @param amount Jumlah pembayaran (minimal 10000)
     * @param payerEmail Email pembeli (opsional untuk sandbox)
     * @param description Deskripsi pembayaran
     * @param customerName Nama customer
     * @param customerPhone Nomor HP customer
     * @return XenditInvoiceResponse atau null jika gagal
     */
    suspend fun createInvoice(
        externalId: String,
        amount: Long,
        payerEmail: String? = null,
        description: String,
        customerName: String,
        customerPhone: String? = null
    ): XenditInvoiceResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // Validasi minimal amount
                if (amount < 10000) {
                    Log.e("XenditHelper", "Amount minimal Rp 10.000")
                    return@withContext null
                }

                val jsonBody = JSONObject().apply {
                    put("external_id", externalId)
                    put("amount", amount)
                    put("description", description)
                    put("invoice_duration", 86400) // 24 jam
                    put("currency", "IDR")

                    // Customer info (untuk sandbox, email opsional)
                    val customer = JSONObject().apply {
                        put("given_names", customerName)
                        payerEmail?.let { put("email", it) }
                        customerPhone?.let { put("mobile_number", it) }
                    }
                    put("customer", customer)

                    // Success/Failure redirect (opsional)
                    // Bisa dikosongkan dulu untuk sandbox
                    // put("success_redirect_url", "https://your-app.com/success")
                    // put("failure_redirect_url", "https://your-app.com/failed")
                }

                val requestBody = jsonBody.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${XenditConfig.BASE_URL}/v2/invoices")
                    .addHeader("Authorization", "Basic ${getEncodedApiKey()}")
                    .post(requestBody)
                    .build()

                Log.d("XenditHelper", "Request Body: $jsonBody")

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("XenditHelper", "Response Code: ${response.code}")
                Log.d("XenditHelper", "Response Body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    parseInvoiceResponse(responseBody)
                } else {
                    Log.e("XenditHelper", "Failed to create invoice: ${response.code} - $responseBody")
                    null
                }
            } catch (e: Exception) {
                Log.e("XenditHelper", "Error creating invoice", e)
                null
            }
        }
    }

    /**
     * Cek status invoice
     */
    suspend fun getInvoiceStatus(invoiceId: String): XenditInvoiceResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${XenditConfig.BASE_URL}/v2/invoices/$invoiceId")
                    .addHeader("Authorization", "Basic ${getEncodedApiKey()}")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    parseInvoiceResponse(responseBody)
                } else {
                    Log.e("XenditHelper", "Failed to get invoice: ${response.code}")
                    null
                }
            } catch (e: Exception) {
                Log.e("XenditHelper", "Error getting invoice", e)
                null
            }
        }
    }

    /**
     * Encode API Key untuk Basic Auth
     */
    private fun getEncodedApiKey(): String {
        return Base64.encodeToString(
            "${XenditConfig.XENDIT_SECRET_KEY}:".toByteArray(),
            Base64.NO_WRAP
        )
    }

    /**
     * Parse response JSON
     */
    private fun parseInvoiceResponse(json: String): XenditInvoiceResponse {
        val jsonObject = JSONObject(json)

        return XenditInvoiceResponse(
            id = jsonObject.getString("id"),
            externalId = jsonObject.getString("external_id"),
            userId = jsonObject.optString("user_id"),
            status = jsonObject.getString("status"),
            merchantName = jsonObject.getString("merchant_name"),
            amount = jsonObject.getLong("amount"),
            payerEmail = jsonObject.optString("payer_email"),
            description = jsonObject.getString("description"),
            invoiceUrl = jsonObject.getString("invoice_url"),
            expiryDate = jsonObject.getString("expiry_date"),
            currency = jsonObject.getString("currency"),
            created = jsonObject.getString("created"),
            updated = jsonObject.getString("updated")
        )
    }
}

/**
 * Data class untuk response Xendit Invoice
 */
data class XenditInvoiceResponse(
    val id: String,
    val externalId: String,
    val userId: String,
    val status: String, // PENDING, PAID, SETTLED, EXPIRED
    val merchantName: String,
    val amount: Long,
    val payerEmail: String?,
    val description: String,
    val invoiceUrl: String,
    val expiryDate: String,
    val currency: String,
    val created: String,
    val updated: String
)
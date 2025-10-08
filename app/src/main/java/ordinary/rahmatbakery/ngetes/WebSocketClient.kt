package ordinary.rahmatbakery.ngetes

import android.content.Context
import android.os.Looper
import android.os.Handler
import android.widget.Toast
import okhttp3.*

class WebSocketClient(url: String, private val context: Context) {
    private val client = OkHttpClient()
    private val request = Request.Builder().url(url).build()
    private var webSocket: WebSocket? = null

    fun connect() {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                showToast("tersambung")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                showToast("pesan : $text")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                showToast("closed. reason : $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                showToast("error : ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }


    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Closed by user")
    }
}
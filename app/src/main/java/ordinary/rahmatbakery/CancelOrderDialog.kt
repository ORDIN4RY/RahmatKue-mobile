package ordinary.rahmatbakery

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import ordinary.rahmatbakery.R

class CancelOrderDialog(
    context: Context,
    private val onConfirm: (String) -> Unit
) : Dialog(context) {

    private lateinit var etAlasan: EditText
    private lateinit var btnBatal: Button
    private lateinit var btnKonfirmasi: Button
    private lateinit var btnClose: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_cancel_order)

        initViews()
        setupListeners()

        // Make dialog fill width
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initViews() {
        etAlasan = findViewById(R.id.etAlasan)
        btnBatal = findViewById(R.id.btnBatal)
        btnKonfirmasi = findViewById(R.id.btnKonfirmasi)
        btnClose = findViewById(R.id.btnClose)
    }

    private fun setupListeners() {
        btnClose.setOnClickListener {
            dismiss()
        }

        btnBatal.setOnClickListener {
            dismiss()
        }

        btnKonfirmasi.setOnClickListener {
            val alasan = etAlasan.text.toString().trim()

            if (alasan.isEmpty()) {
                Toast.makeText(context, "Harap isi alasan pembatalan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (alasan.length < 10) {
                Toast.makeText(context, "Alasan minimal 10 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onConfirm(alasan)
            dismiss()
        }
    }
}
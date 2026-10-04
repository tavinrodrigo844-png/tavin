package com.tavin.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tavin.app.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var mode: String = "sender"
    private var serverUrl: String = "ws://192.168.0.10:8080"
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val handler = Handler(Looper.getMainLooper())

    private val captureRunnable = object : Runnable {
        override fun run() {
            if (mode == "sender") captureAndSendFrame()
            handler.postDelayed(this, 100) // ~10 FPS
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSender.isChecked = true
        binding.btnSender.setOnClickListener { mode = "sender" }
        binding.btnReceiver.setOnClickListener { mode = "receiver" }

        binding.connectButton.setOnClickListener {
            val input = binding.serverInput.text?.toString()?.trim().orEmpty()
            if (input.isBlank()) {
                Toast.makeText(this, "Informe o endereço do servidor.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            serverUrl = if (input.startsWith("ws://") || input.startsWith("wss://")) input else "ws://$input"
            connectWebSocket()
        }

        binding.startButton.setOnClickListener {
            if (mode == "sender") {
                startSenderMode()
            } else {
                startReceiverMode()
            }
        }
    }

    private fun connectWebSocket() {
        binding.statusText.text = "Status: conectando ao servidor $serverUrl"

        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                runOnUiThread {
                    binding.statusText.text = "Status: conectado"
                    webSocket.send("{\"type\":\"register\",\"role\":\"${mode}\"}")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    if (type == "frame") {
                        val base64 = json.optString("data")
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            runOnUiThread {
                                binding.previewImage.setImageBitmap(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Tavin", "Erro ao decodificar frame", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                runOnUiThread {
                    binding.statusText.text = "Status: erro de conexão"
                    Toast.makeText(this@MainActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun startSenderMode() {
        val mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 1001)
    }

    private fun startReceiverMode() {
        binding.statusText.text = "Status: aguardando transmissão"
        if (webSocket != null) {
            webSocket?.send("{\"type\":\"ping\"}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            val mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val dpi = metrics.densityDpi

            imageReader = ImageReader.newInstance(width, height, ImageFormat.RGBA_8888, 2)

            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "TavinDisplay",
                width,
                height,
                dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )

            binding.statusText.text = "Status: capturando tela"
            handler.post(captureRunnable)
        }
    }

    private fun captureAndSendFrame() {
        val reader = imageReader ?: return
        val image = reader.acquireLatestImage() ?: return

        try {
            val planes = image.planes
            if (planes.isEmpty()) return

            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width

            val pixels = IntArray(image.width * image.height)
            var index = 0

            val rowLength = image.width * 4
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    val alpha = buffer.get().toInt() and 0xFF
                    val red = buffer.get().toInt() and 0xFF
                    val green = buffer.get().toInt() and 0xFF
                    val blue = buffer.get().toInt() and 0xFF
                    pixels[index++] = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
                }
                if (rowPadding > 0) {
                    buffer.position(buffer.position() + rowPadding)
                }
            }

            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            val bytes = stream.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            webSocket?.send("{\"type\":\"frame\",\"data\":\"$base64\"}")
        } catch (e: Exception) {
            Log.e("Tavin", "Erro ao capturar frame", e)
        } finally {
            image.close()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        virtualDisplay?.release()
        mediaProjection?.stop()
        webSocket?.close(1000, "Aplicativo encerrado")
        super.onDestroy()
    }
}


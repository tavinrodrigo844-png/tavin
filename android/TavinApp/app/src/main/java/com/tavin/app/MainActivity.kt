package com.tavin.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tavin.app.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.nio.ByteBuffer
import kotlin.math.min

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
            if (mode == "sender") {
                captureAndSendFrame()
            }
            handler.postDelayed(this, 100)
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
            val input = binding.serverInput.text?.toString()?.trim() ?: ""
            if (input.isBlank()) {
                Toast.makeText(this, "Informe o endereço do servidor.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val normalized = if (input.startsWith("ws://") || input.startsWith("wss://")) input else "ws://$input"
            serverUrl = normalized
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
                    Toast.makeText(this@MainActivity, "Conectado", Toast.LENGTH_SHORT).show()
                    webSocket.send("{\"type\":\"register\",\"role\":\"${mode}\"}")
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = text
                if (message.contains("\"type\":\"frame\"")) {
                    val payload = message.substringAfter("\"data\":\"").substringBefore('"')
                    val bytes = Base64.decode(payload, Base64.DEFAULT)
                    val bitmap = BitmapFactoryCompat.decodeByteArray(bytes)
                    if (bitmap != null) {
                        runOnUiThread {
                            binding.previewImage.setImageBitmap(bitmap)
                        }
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                runOnUiThread {
                    binding.statusText.text = "Status: fechando conexão"
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
        handler.postDelayed({
            if (webSocket != null) {
                webSocket?.send("{\"type\":\"ping\"}")
            }
        }, 500)
    }

    @SuppressLint("WrongConstant")
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
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width
            val bitmap = Bitmap.createBitmap(
                image.width + (rowPadding / pixelStride),
                image.height,
                Bitmap.Config.ARGB_8888
            )

            val pixels = IntArray(image.width * image.height)
            var index = 0
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    val pixel = buffer.getInt()
                    pixels[index++] = pixel
                }
                if (rowPadding > 0) {
                    buffer.position(buffer.position() + rowPadding)
                }
            }

            bitmap.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
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

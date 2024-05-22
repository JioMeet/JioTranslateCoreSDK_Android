package com.jio.jiotranslatecoresdk

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    var wavFilePath by mutableStateOf("")

    @SuppressLint("MissingPermission")
    fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread {
            writeAudioDataToWavFile()
        }
        recordingThread?.start()
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread = null
    }

    private fun writeAudioDataToWavFile() {
        val data = ByteArray(bufferSize)
        var outputStream: FileOutputStream? = null

        try {
            val file = File(FileUtility.getFileDirectory(context), "recording.wav")
            wavFilePath = file.absolutePath
            outputStream = FileOutputStream(file)
            writeWavHeader(outputStream, channelConfig, sampleRate, audioFormat)

            while (isRecording) {
                val read = audioRecord?.read(data, 0, bufferSize) ?: 0
                if (read > 0) {
                    outputStream.write(data, 0, read)
                }
            }

            updateWavHeader(outputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }
    }

    private fun writeWavHeader(outputStream: FileOutputStream, channels: Int, sampleRate: Int, audioFormat: Int) {
        val byteRate = sampleRate * 16 * channels / 8
        val buffer = ByteBuffer.allocate(44)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray())
        buffer.putInt(0) // Placeholder for chunk size
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Sub-chunk size
        buffer.putShort(1) // Audio format (PCM)
        buffer.putShort(1) // Number of channels
        buffer.putInt(sampleRate) // Sample rate
        buffer.putInt(byteRate) // Byte rate
        buffer.putShort((16 * channels / 8).toShort()) // Block align
        buffer.putShort(16.toShort()) // Bits per sample
        buffer.put("data".toByteArray())
        buffer.putInt(0) // Placeholder for data size
        buffer.flip()

        outputStream.channel.write(buffer)
    }

    private fun updateWavHeader(outputStream: FileOutputStream?) {
        if (outputStream == null) return

        val totalAudioLen = outputStream.channel.size() - 44
        val totalDataLen = totalAudioLen + 36

        val buffer = ByteBuffer.allocate(8)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(totalDataLen.toInt())
        buffer.flip()
        outputStream.channel.position(4)
        outputStream.channel.write(buffer)

        buffer.clear()
        buffer.putInt(totalAudioLen.toInt())
        buffer.flip()
        outputStream.channel.position(40)
        outputStream.channel.write(buffer)
    }
}
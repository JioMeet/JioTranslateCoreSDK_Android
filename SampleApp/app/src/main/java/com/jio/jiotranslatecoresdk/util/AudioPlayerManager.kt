package com.jio.jiotranslatecoresdk.util

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.jio.jiotranslate.di.JioTranslateKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AudioPlayerManager {
    const val TAG = "AudioPlayerManager"
    private var coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var exoPlayer: ExoPlayer? = null
    private var onStopPlayingCallback: ((Unit) -> Unit)? = null

    private var progressUpdateJob: Job? = null
    private const val progressUpdateIntervalMillis = 1000L // Update progress every second

    private var onProgressUpdateListener: (Long, Long) -> Unit = { currentPosition, duration ->
        // Handle progress update here
        val progressPercentage = (currentPosition.toFloat() / duration.toFloat()) * 100
        Log.d(TAG, ": $currentPosition ms, Duration: $duration ms, Progress: $progressPercentage%")
    }
    // Function to set the progress listener
    fun setProgressListener(listener: (Long, Long) -> Unit) {
        onProgressUpdateListener = listener
        startProgressUpdates()
    }

    // Function to start the periodic progress updates
    private fun startProgressUpdates() {
        progressUpdateJob?.cancel() // Cancel any existing job

        progressUpdateJob = coroutineScope.launch {
            while (true) {
                val duration = exoPlayer?.duration ?: C.TIME_UNSET
                val position = exoPlayer?.currentPosition ?: C.TIME_UNSET
                if (duration != C.TIME_UNSET && position != C.TIME_UNSET && exoPlayer?.isPlaying == true) {
                    onProgressUpdateListener.invoke(position, duration)
                }
                delay(progressUpdateIntervalMillis)
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun playAudioBytes(audioBytes: ByteArray, onStopPlaying: (Unit) -> Unit) {
        onStopPlayingCallback = onStopPlaying
        coroutineScope.launch {
            exoPlayer?.stop()
            exoPlayer = ExoPlayer.Builder(JioTranslateKit.appContext).build()
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
            exoPlayer?.setAudioAttributes(audioAttributes, true)
            playWithAudioTrack(audioBytes)
        }
    }

    @UnstableApi
    private fun playWithAudioTrack(audioBytes: ByteArray) {
        val mediaItem =
            MediaItem.fromUri(Uri.EMPTY) // Since we're using ByteArrayDataSource, we can set any URI.
        val dataSourceFactory = DataFactory(audioBytes)
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)
        exoPlayer?.addListener(playerListener)
        exoPlayer?.setMediaSource(mediaSource)
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                onStopPlayingCallback?.invoke(Unit)
            }
        }
    }

    fun pausePlayback() {
        exoPlayer?.pause()
    }

    fun resumePlayback() {
        exoPlayer?.play()
    }

    fun replay() {
        exoPlayer?.seekTo(0)
        exoPlayer?.play()
    }

    fun stopPlayback() {
        coroutineScope.launch {
            exoPlayer?.stop()
        }
    }

    @UnstableApi
    private class DataFactory(private val audioBytes: ByteArray) : DataSource.Factory {
        override fun createDataSource(): DataSource {
            return ByteArrayDataSource(audioBytes)
        }
    }
}

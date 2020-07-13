package com.example.histogram

import android.content.res.AssetFileDescriptor
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_demo.*
import java.nio.ByteBuffer

class DemoActivity : AppCompatActivity() {

    private lateinit var audioPlayer: AudioPlayerUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        audioPlayer = AudioPlayerUtil()
        getHistogramData()
    }

    private fun getHistogramData() {
        val extractor = MediaExtractor();
        val afd: AssetFileDescriptor =
            this.resources.openRawResourceFd(R.raw.silence1sec_tone1sec)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            extractor.setDataSource(afd)
            val numTracks = extractor.trackCount

            for (i in 0 until numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                Log.d("logd", "getHistogramData: track: $i format: $format mime: $mime")
            }

            extractor.selectTrack(0)

            Log.d("logd", "getHistogramData: ");

            val samplesList = mutableListOf<ByteArray>()
            var allSamplesAsByteArray = ByteArray(0)

            val inputBuffer = ByteBuffer.allocate(2048*2048)
            while (extractor.readSampleData(inputBuffer, 0) >= 0) {
                val trackIndex = extractor.sampleTrackIndex
                val presentationTimeUs = extractor.sampleTime

                val currentSample = ByteArray(inputBuffer.remaining())
                inputBuffer.get(currentSample)

                samplesList.add(currentSample)
                allSamplesAsByteArray += currentSample

                inputBuffer.clear()
                extractor.advance()
            }

            mixedBarVisualizerPanel.audioBytes = allSamplesAsByteArray

            extractor.release();
        }
    }

    override fun onStart() {
        super.onStart()
        //   startPlayingAudio(R.raw.beethoven_12_variation)
    }

    override fun onStop() {
        super.onStop()
        //    stopPlayingAudio();
    }

    private fun startPlayingAudio(@RawRes resId: Int) {
        audioPlayer.play(this, resId) {}
        audioPlayer.getAudioSessionId()
            ?.also {
                mixedBarVisualizerPanel.setAudioSessionId(it)
            }
    }

    private fun stopPlayingAudio() {
        audioPlayer.stop();
        mixedBarVisualizerPanel.release()
    }
}
package com.cellhubs.democameraapp

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cellhubs.democameraapp.widgets.TouchableImageView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.spherical.SphericalSurfaceView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), PlayerControlView.VisibilityListener, PlaybackPreparer {
    companion object {
        const val SPHERICAL_STEREO_MODE_EXTRA = "spherical_stereo_mode"
        const val SPHERICAL_STEREO_MODE_MONO = "mono"
        const val SPHERICAL_STEREO_MODE_TOP_BOTTOM = "top_bottom"
        const val SPHERICAL_STEREO_MODE_LEFT_RIGHT = "left_right"
    }

    private lateinit var soundPool: SoundPool
    private lateinit var audioManager: AudioManager
    // Maximumn sound stream.
    private val MAX_STREAMS = 5
    // Stream type.
    private val streamType = AudioManager.STREAM_MUSIC
    private var isSoundIdStickerLoaded: Boolean = false
    private var soundIdSticker: Int = 0
    private var volume: Float = 0.toFloat()
    private var finalMediaSource: MediaSource? = null
    private var exoPlayer: SimpleExoPlayer? = null
    private var stickers: MutableList<StickerMedia> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSoundPool()
        initPlayerView()
        tvAddSticker.setOnClickListener {
            rlChildViews.addView(TouchableImageView(this).apply {
                Glide.with(this).load(Uri.parse("file:///android_asset/gif/sticker.gif")).into(this)
            })
            // Get Stickers
            playSoundSticker()
        }


        tvAddMusic.setOnClickListener {
            playAudio()
        }

        btnExport.setOnClickListener {
        }
    }

    private fun initSoundPool() {
        // AudioManager audio settings for adjusting the volume
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Current volumn Index of particular stream type.
        val currentVolumeIndex = audioManager.getStreamVolume(streamType).toFloat()
        // Get the maximum volume index for a particular stream type.
        val maxVolumeIndex = audioManager.getStreamMaxVolume(streamType).toFloat()
        // Volumn (0 --> 1)
        volume = currentVolumeIndex / maxVolumeIndex
        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.volumeControlStream = streamType
        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21) {
            val audioAttrib = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val builder = SoundPool.Builder()
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS)
            this.soundPool = builder.build()
        } else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0)
        }// for Android SDK < 21

        // When Sound Pool load complete.
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            when (sampleId) {
                soundIdSticker -> {
                    isSoundIdStickerLoaded = true
                }
            }
        }
        // Load sound file (destroy.wav) into SoundPool.
        soundIdSticker = soundPool.load(this, R.raw.source_effect, 1)
    }

    private fun playSoundSticker() {
        if (isSoundIdStickerLoaded) {
            val leftVolumn = volume
            val rightVolumn = volume
            // Play sound objects destroyed. Returns the ID of the new stream.
            soundIdSticker = soundPool.play(soundIdSticker, leftVolumn, rightVolumn, 1, 0, 1f)
        }
    }

    private fun initPlayerView() {
        val sphericalStereoMode = intent.getStringExtra(SPHERICAL_STEREO_MODE_EXTRA)
        if (sphericalStereoMode != null) {
            val stereoMode: Int = when {
                SPHERICAL_STEREO_MODE_MONO.equals(sphericalStereoMode) -> C.STEREO_MODE_MONO
                SPHERICAL_STEREO_MODE_TOP_BOTTOM.equals(sphericalStereoMode) -> C.STEREO_MODE_TOP_BOTTOM
                SPHERICAL_STEREO_MODE_LEFT_RIGHT.equals(sphericalStereoMode) -> C.STEREO_MODE_LEFT_RIGHT
                else -> {
                    finish()
                    return
                }
            }
            (playerView.videoSurfaceView as SphericalSurfaceView).setDefaultStereoMode(stereoMode)
        }
        playerView.run {
            setControllerVisibilityListener(this@MainActivity)
        }
        playVideo()
    }


    private fun playVideo() {
        // Init Player
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
        exoPlayer?.playWhenReady = true
        playerView.player = exoPlayer
        playerView.setPlaybackPreparer(this)
        finalMediaSource =
            ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayerInfo")))
                .setExtractorsFactory(DefaultExtractorsFactory())
                .createMediaSource(Uri.parse("asset:///video/demo.MP4"))
        exoPlayer?.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer?.prepare(finalMediaSource)
    }

    private fun playAudio() {
        val audioMediaSource =
            ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayerInfo")))
                .setExtractorsFactory(DefaultExtractorsFactory())
                .createMediaSource(Uri.parse("asset:///musics/out.mp3"))
        val mergedAudioSource = MergingMediaSource(
            ClippingMediaSource(finalMediaSource, 0, 8_000_000),
            audioMediaSource
        )
        exoPlayer?.prepare(mergedAudioSource)
    }


    override fun preparePlayback() {
    }

    override fun onVisibilityChange(visibility: Int) {
    }

}

package com.cellhubs.democameraapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.cellhubs.democameraapp.widgets.TouchableImageView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.spherical.SphericalSurfaceView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.jaygoo.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), PlayerControlView.VisibilityListener, PlaybackPreparer {
    companion object {
        const val SPHERICAL_STEREO_MODE_EXTRA = "spherical_stereo_mode"
        const val SPHERICAL_STEREO_MODE_MONO = "mono"
        const val SPHERICAL_STEREO_MODE_TOP_BOTTOM = "top_bottom"
        const val SPHERICAL_STEREO_MODE_LEFT_RIGHT = "left_right"
        private const val PICK_IMAGE_REQUEST_CODE = 12312
        private const val PICK_VIDEO_REQUEST_CODE = 12313
        private const val DEFAUTLT_VIDEO_LIMIT_TIME = 8000L
    }

    private lateinit var soundPool: SoundPool
    private lateinit var audioManager: AudioManager
    // Maximumn sound stream.
    private val MAX_STREAMS = 5
    // Stream type.
    private val streamType = AudioManager.STREAM_MUSIC
    private var isSoundIdStickerLoaded: Boolean = false
    private var soundIdSticker: Int = 0
    private var volume = 0f
    private var finalMediaSource: MediaSource? = null
    private var stickers: MutableList<StickerMedia> = arrayListOf()
    private lateinit var exoPlayer: ExoPlayer
    private var isOpenVideo = false
    private var isSetValueSeekBar = false
    private var isGetVideoDuration = false
    private var isLeft = false
    private var uriFile: Uri? = null
    private var startTime = 0L
    private var endTime = DEFAUTLT_VIDEO_LIMIT_TIME
    private var duration = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initializePlayer()
        initPlayerView()
        initSoundPool()
        initListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST_CODE -> data?.data?.let { uri ->
                    rlChildViews.addView(TouchableImageView(this@MainActivity).apply {
                        Glide.with(this)
                            .load(uri)
                            .into(this)
                    })
                }
                else -> data?.data?.let { uri ->
                    playVideoFromDevice(uri)
                }
            }
        }
    }

    private fun initViews() {
        if (isOpenVideo) {
            imgAddVideo.visibility = View.GONE
            playerView.visibility = View.VISIBLE
        } else {
            playerView.visibility = View.GONE
            imgAddVideo.visibility = View.VISIBLE
        }
    }

    private fun initializePlayer() {
        // Init ExoPlayer
        exoPlayer = ExoPlayerFactory.newSimpleInstance(
            this,
            DefaultRenderersFactory(this),
            DefaultTrackSelector(),
            DefaultLoadControl()
        )
    }

    private fun initListener() {
//        btnAddPhoto.setOnClickListener {
//            openFolderByType(
//                "image/*",
//                "Select Image",
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                PICK_IMAGE_REQUEST_CODE
//            )
//        }

        imgAddVideo.setOnClickListener {
            openFolderByType(
                "video/*",
                "Select Video",
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                PICK_VIDEO_REQUEST_CODE
            )
        }

        btnOK.setOnClickListener {
            exoPlayer.prepare(ClippingMediaSource(finalMediaSource, startTime * 1000, endTime * 1000))
        }

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

    private fun openFolderByType(types: String, message: String, uri: Uri, code: Int) {
        val chooserIntent = Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
            type = types
        }, message)
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            arrayOf(
                Intent(Intent.ACTION_PICK, uri).apply {
                    type = types
                })
        )

        startActivityForResult(chooserIntent, code)
    }

    private fun playVideoFromDevice(uri: Uri) {
        uriFile = uri

        finalMediaSource = ExtractorMediaSource
            .Factory(DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayerInfo")))
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(uri)

        exoPlayer.prepare(finalMediaSource)

        exoPlayer.playWhenReady = true
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                isOpenVideo = playWhenReady
                initViews()
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // media actually playing
                    if (!isGetVideoDuration) {
                        isGetVideoDuration = true
                        duration = exoPlayer.duration
                    }
                    handleSeekBar()
                } else if (playWhenReady) {
                    // might be idle (plays after prepare()),
                    // buffering (plays when data available)
                    // or ended (plays when seek away from end)
                } else {
                    // player paused in any state
                }
            }
        })

        // Play Video
        playerView.player = exoPlayer
        playerView.useController = false
    }

    private fun handleSeekBar() {
        rangeSeekBar.run {
            setRange(0f, duration.toFloat())

            // Max and min value
            tickMarkMode = RangeSeekBar.TRICK_MARK_MODE_OTHER
            tickMarkTextArray = arrayOf(milliSecondToString(0), milliSecondToString(duration))

            if (!isSetValueSeekBar) {
                setValue(startTime.toFloat(), DEFAUTLT_VIDEO_LIMIT_TIME.toFloat())
                isSetValueSeekBar = true
            }

            // Show indicator mode
            leftSeekBar.indicatorShowMode = SeekBar.INDICATOR_MODE_SHOW_WHEN_TOUCH
            rightSeekBar.indicatorShowMode = SeekBar.INDICATOR_MODE_SHOW_WHEN_TOUCH

            // Listener
            setOnRangeChangedListener(object : OnRangeChangedListener {
                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                    this@MainActivity.isLeft = isLeft
                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                    exoPlayer.seekTo(if (isLeft) startTime else endTime)
                }

                override fun onRangeChanged(
                    view: RangeSeekBar?,
                    leftValue: Float,
                    rightValue: Float,
                    isFromUser: Boolean
                ) {
                    Log.d("zxc ", "$leftValue === $rightValue")
                    startTime = leftValue.toLong()
                    endTime = rightValue.toLong()

                    if (isLeft)
                        leftSeekBar.setIndicatorText(milliSecondToString(startTime))
                    else
                        rightSeekBar.setIndicatorText(milliSecondToString(endTime))

                    // Set range limit of seekbar
                    if (endTime - startTime > DEFAUTLT_VIDEO_LIMIT_TIME && !isLeft) {
                        startTime = endTime - DEFAUTLT_VIDEO_LIMIT_TIME
                        setValue(startTime.toFloat(), endTime.toFloat())
                    } else if (startTime + DEFAUTLT_VIDEO_LIMIT_TIME < endTime && isLeft) {
                        endTime = startTime + DEFAUTLT_VIDEO_LIMIT_TIME
                        setValue(startTime.toFloat(), endTime.toFloat())
                    }
                }
            })
        }
    }

    private fun milliSecondToString(duration: Long): String {
        var millisecond = duration
        millisecond /= 1000
        return (if (millisecond / 60 < 10) "0" else "") + millisecond / 60 + ":" + (if (millisecond % 60 < 10) "0" else "") + millisecond % 60
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
                SPHERICAL_STEREO_MODE_MONO == sphericalStereoMode -> C.STEREO_MODE_MONO
                SPHERICAL_STEREO_MODE_TOP_BOTTOM == sphericalStereoMode -> C.STEREO_MODE_TOP_BOTTOM
                SPHERICAL_STEREO_MODE_LEFT_RIGHT == sphericalStereoMode -> C.STEREO_MODE_LEFT_RIGHT
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
    }

    private fun playAudio() {
        val audioMediaSource =
            ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayerInfo")))
                .setExtractorsFactory(DefaultExtractorsFactory())
                .createMediaSource(Uri.parse("asset:///musics/out.mp3"))
        val mergedAudioSource = MergingMediaSource(
            ClippingMediaSource(finalMediaSource, startTime * 1000, endTime * 1000),
            audioMediaSource
        )
        exoPlayer.prepare(mergedAudioSource)
    }

    override fun preparePlayback() {
    }

    override fun onVisibilityChange(visibility: Int) {
    }

}

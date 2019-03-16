package com.cellhubs.democameraapp

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.cellhubs.democameraapp.widgets.TouchableImageView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 12312
        private const val PICK_VIDEO_REQUEST_CODE = 12313
    }

    private lateinit var exoPlayer: ExoPlayer
    private var isOpenVideo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initializePlayer()
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
            exoPlayerView.visibility = View.VISIBLE
        } else {
            exoPlayerView.visibility = View.GONE
            imgAddVideo.visibility = View.VISIBLE
        }
    }

    private fun initializePlayer() {
        // Init ExoPlayer
        val trackSelector = DefaultTrackSelector()
        val loadControl = DefaultLoadControl()
        val renderersFactory = DefaultRenderersFactory(this)

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector, loadControl)
    }

    private fun initListener() {
        btnAddPhoto.setOnClickListener {
            openFolderByType(
                "image/*",
                "Select Image",
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PICK_IMAGE_REQUEST_CODE
            )
        }

        imgAddVideo.setOnClickListener {
            openFolderByType(
                "video/*",
                "Select Video",
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                PICK_VIDEO_REQUEST_CODE
            )
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
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))

        val mediaSource = ExtractorMediaSource
            .Factory(DefaultDataSourceFactory(this, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(uri)

        exoPlayer.prepare(mediaSource)

        exoPlayer.playWhenReady = true
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                isOpenVideo = playWhenReady
                initViews()
            }
        })

        // Play Video
        exoPlayerView.player = exoPlayer
        exoPlayerView.controllerAutoShow = false

        Log.d("zxc ", "${locateView(exoPlayerView)} === ${locateView(exoPlayerView).width()}")
    }

    fun locateView(view: View?): Rect {
        val loc = Rect()
        val location = IntArray(2)
        if (view == null) {
            return loc
        }
        view.getLocationOnScreen(location)

        loc.left = location[0]
        loc.top = location[1]
        loc.right = loc.left + view.width
        loc.bottom = loc.top + view.height
        return loc
    }
}

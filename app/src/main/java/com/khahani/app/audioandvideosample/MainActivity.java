package com.khahani.app.audioandvideosample;

import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    ImageView playButton, pauseButton;
    SeekBar seekBar;

    PlaybackStateCompat playbackStateCompat;

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    super.onConnected();
                    try {
                        MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
                        mMediaController =
                                new MediaControllerCompat(MainActivity.this, token);

                        MediaControllerCompat.setMediaController(MainActivity.this, mMediaController);

                        mMediaController.registerCallback(
                                new MediaControllerCompat.Callback() {
                                    @Override
                                    public void onPlaybackStateChanged(PlaybackStateCompat state) {

                                        playbackStateCompat = state;

                                        switch (state.getState()) {
                                            case PlaybackStateCompat.STATE_ERROR:
                                                Toast.makeText(MainActivity.this, "Something wrong happen", Toast.LENGTH_SHORT).show();
                                                break;
                                            case PlaybackStateCompat.STATE_PLAYING:
                                                runSeekbar();
                                                playButton.setVisibility(View.INVISIBLE);
                                                pauseButton.setVisibility(View.VISIBLE);
                                                break;
                                            case PlaybackStateCompat.STATE_PAUSED:
                                                playButton.setVisibility(View.VISIBLE);
                                                pauseButton.setVisibility(View.INVISIBLE);
                                                break;
                                            case PlaybackStateCompat.STATE_STOPPED:
                                                stopSeekbar();
                                                seekBar.setProgress(0);
                                                playButton.setVisibility(View.VISIBLE);
                                                pauseButton.setVisibility(View.INVISIBLE);
                                                break;
                                        }
                                    }

                                    @Override
                                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                                        if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_DURATION)) {
                                            int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                                            try {
                                                seekBar.setMax(duration);
                                            } catch (Exception e) {
                                                Log.e(TAG, "onMetadataChanged: " + e.getMessage());
                                            }
                                        }
                                    }
                                });

                        mMediaController.getTransportControls()
                                .sendCustomAction(MediaPlaybackService.ACTION_UPDATE_META_DATA, null);

                    } catch (RemoteException e) {
                        Log.e(TAG, "onConnected: " + e.getMessage());
                    }
                }

                @Override
                public void onConnectionSuspended() {
                    super.onConnectionSuspended();
                }

                @Override
                public void onConnectionFailed() {
                    super.onConnectionFailed();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MediaPlaybackService.class),
                mMediaBrowserCallback,
                null);

        mMediaBrowser.connect();

        playButton = findViewById(R.id.imageViewPlay);
        pauseButton = findViewById(R.id.imageViewPause);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.getTransportControls().play();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.getTransportControls().pause();
            }
        });

        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar,
                                              int progress, boolean fromUser) {

                    switch (playbackStateCompat.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING:
                        case PlaybackStateCompat.STATE_PAUSED:
                            if (fromUser) {
                                mMediaController.getTransportControls()
                                        .seekTo(progress * 1000);
                            }
                            break;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    private Handler seekbarHandler;
    private Runnable seekbarRunnable;

    private void runSeekbar() {
        if (seekbarHandler == null) {
            seekBar.setEnabled(true);
            seekbarHandler = new Handler();
            seekbarRunnable = new Runnable() {
                @Override
                public void run() {
                    if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                        int mCurrentPosition = (int) playbackStateCompat.getPosition() / 1000;

                        seekBar.setProgress(mCurrentPosition);
                    }
                    seekbarHandler.postDelayed(this, 1000);
                }
            };
            runOnUiThread(seekbarRunnable);
        }
    }

    private void stopSeekbar() {
        seekBar.setEnabled(false);
        seekbarHandler.removeCallbacks(seekbarRunnable);
        seekbarHandler = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playbackStateCompat != null){
            if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PAUSED){
                mMediaController.getTransportControls().stop();
            }
        }
        mMediaBrowser.disconnect();
    }
}

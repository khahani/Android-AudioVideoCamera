package com.khahani.app.audioandvideosample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mMediaSession;

    MediaControllerCompat mediaController;

    ImageView playButton, pauseButton;
    SeekBar seekBar;

    private MediaPlayer.OnPreparedListener myOnPreparedListener =
            new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    seekBar.setMax(mediaPlayer.getDuration() / 1000);

                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

                    int result = audioManager.requestAudioFocus(focusChangeListener,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                        registerNoisyReceiver();
                        mMediaSession.setActive(true);
                        updatePlaybackState();
                        //mediaPlayer.start();
                        mediaController.getTransportControls().play();

                    }

                }
            };

    private MediaPlayer.OnCompletionListener completionListener =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    audioManager.abandonAudioFocus(focusChangeListener);

                    mediaController.getTransportControls().stop();

                }
            };

    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {

                    AudioManager am =
                            (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    switch (focusChange) {
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                            mediaPlayer.setVolume(0.2f, 0.2f);
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                            pauseAudioPlayback();
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS):
                            //mediaPlayer.stop();
                            mediaController.getTransportControls().stop();
                            am.abandonAudioFocus(this);
                            break;
                        case (AudioManager.AUDIOFOCUS_GAIN):
                            mediaPlayer.setVolume(1f, 1f);
                            //mediaPlayer.start();
                            mediaController.getTransportControls().play();
                            break;
                        default:
                            break;
                    }
                }
            };

    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals
                    (intent.getAction())) {
                pauseAudioPlayback();
            }
        }
    }

    private void pauseAudioPlayback() {
        //mediaPlayer.pause();
        mediaController.getTransportControls().pause();
    }

    NoisyAudioStreamReceiver mNoisyAudioStreamReceiver
            = new NoisyAudioStreamReceiver();

    private void registerNoisyReceiver() {
        IntentFilter filter =
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyAudioStreamReceiver, filter);
    }

    public void unregisterNoisyReceiver() {
        unregisterReceiver(mNoisyAudioStreamReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mMediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaController =
                new MediaControllerCompat(this, mMediaSession);

        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );

        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();

                if (mediaPlayer == null){
                    initialMediaPlayer();
                }else {

                    mediaPlayer.start();

                    AnimatedVectorDrawableCompat anim =
                            AnimatedVectorDrawableCompat.create(
                                    MainActivity.this,
                                    R.drawable.anim_vector_drawable);
                    playButton.setImageDrawable(anim);
                    Animatable animatable = (Animatable) playButton.getDrawable();
                    animatable.start();

                    playButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);

                    runSeekbar();
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                mediaPlayer.pause();
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);

                stopSeekbar();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
                mediaPlayer.seekTo((int) position);
            }

            @Override
            public void onStop() {
                super.onStop();
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                stopSeekbar();
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
                seekBar.setProgress(0);
            }

        });

        initialMediaPlayer();

        playButton = findViewById(R.id.imageViewPlay);
        pauseButton = findViewById(R.id.imageViewPause);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaController.getTransportControls().play();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaController.getTransportControls().pause();
            }
        });

        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

    }

    private void initialMediaPlayer() {
        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource("http://sv.blogmusic.ir/myahang/Classic-music-1.mp3");
            mediaPlayer.setOnPreparedListener(myOnPreparedListener);
            mediaPlayer.setOnCompletionListener(completionListener);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mediaPlayer != null && fromUser){
                        mediaPlayer.seekTo(progress * 1000);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    PlaybackStateCompat.Builder playbackStateBuilder =
            new PlaybackStateCompat.Builder();

    public void updatePlaybackState() {

        playbackStateBuilder
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_STOP |
                                PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        0, // Track position in ms
                        1.0f); // Playback speed

        mMediaSession.setPlaybackState(playbackStateBuilder.build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNoisyReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerNoisyReceiver();
    }

    Bitmap artworkthumbnail = null;
    String fullSizeArtWorkUri =
            Uri.parse("http://sv.blogmusic.ir/myahang/Classic-music-1.mp3")
                    .toString();
    long duration;
    String album, artist, title;

    public void updateMetadata() {
        MediaMetadataCompat.Builder builder =
                new MediaMetadataCompat.Builder();

        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                artworkthumbnail);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                fullSizeArtWorkUri);

        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);

        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);


        mMediaSession.setMetadata(builder.build());
    }

    private Handler seekbarHandler;
    private Runnable seekbarRunnable;

    private void runSeekbar() {
        seekBar.setEnabled(true);
        seekbarHandler = new Handler();
        seekbarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                seekbarHandler.postDelayed(this, 1000);
            }
        };
        runOnUiThread(seekbarRunnable);
    }

    private void stopSeekbar() {
        seekBar.setEnabled(false);
        seekbarHandler.removeCallbacks(seekbarRunnable);
    }
}

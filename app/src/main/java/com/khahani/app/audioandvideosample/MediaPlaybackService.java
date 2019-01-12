package com.khahani.app.audioandvideosample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String LOG_TAG =
            MediaPlaybackService.class.getSimpleName();
    public static final String ACTION_UPDATE_META_DATA = "action_update_meta_data";

    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mMediaSession;

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaSession = new MediaSessionCompat(this, LOG_TAG);

        setSessionToken(mMediaSession.getSessionToken());

        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );

        mMediaSession.setCallback(new MediaSessionCompat.Callback() {

            @Override
            public void onPlay() {
                super.onPlay();

                if (mediaPlayer == null) {
                    initialMediaPlayer();
                } else {
                    play();
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                mediaPlayer.pause();
                updatePlaybackState();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
                mediaPlayer.seekTo((int) position);
                updatePlaybackState();
            }

            @Override
            public void onStop() {
                super.onStop();
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                updatePlaybackState();
                stopSelf();
            }

            @Override
            public void onCustomAction(String action, Bundle extras) {
                super.onCustomAction(action, extras);
                if (mediaPlayer != null) {
                    if (action.equals(ACTION_UPDATE_META_DATA)) {
                        updateMetadata();
                    }
                }
            }
        });

        initialMediaPlayer();
    }

    private void play() {
        AudioManager audioManager =
                (AudioManager) getSystemService(AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            registerNoisyReceiver();
            mMediaSession.setActive(true);
            mediaPlayer.start();
            updatePlaybackState();

            startService(new Intent(MediaPlaybackService.this,
                    MediaPlaybackService.class));
        }
    }

    private MediaPlayer.OnPreparedListener myOnPreparedListener =
            new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer = mp;
                    updateMetadata();
                    play();
                }
            };

    private MediaPlayer.OnCompletionListener completionListener =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaplayer) {
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    audioManager.abandonAudioFocus(focusChangeListener);

                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    updatePlaybackState();

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
                            mediaPlayer.stop();

                            am.abandonAudioFocus(this);
                            updatePlaybackState();
                            break;
                        case (AudioManager.AUDIOFOCUS_GAIN):
                            mediaPlayer.setVolume(1f, 1f);
                            mediaPlayer.start();
                            updatePlaybackState();
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
        mediaPlayer.pause();
        updatePlaybackState();
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

    private void initialMediaPlayer() {
        try {

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource("http://sv.blogmusic.ir/myahang/Classic-music-1.mp3");
            mediaPlayer.setOnPreparedListener(myOnPreparedListener);
            mediaPlayer.setOnCompletionListener(completionListener);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    PlaybackStateCompat.Builder playbackStateBuilder =
            new PlaybackStateCompat.Builder();

    public void updatePlaybackState() {

        playbackStateBuilder
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_STOP |
                                PlaybackStateCompat.ACTION_SEEK_TO);

        if (mediaPlayer == null) {
            playbackStateBuilder.setState(
                    PlaybackStateCompat.STATE_STOPPED,
                    0L,
                    1.0f);

        } else {
            try {
                playbackStateBuilder.setState(
                        mediaPlayer.isPlaying() ?
                                PlaybackStateCompat.STATE_PLAYING :
                                PlaybackStateCompat.STATE_PAUSED,
                        mediaPlayer.getCurrentPosition(), // Track position in ms
                        1.0f); // Playback speed
            } catch (Exception e) {
                playbackStateBuilder.setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        0L,
                        1.0f);
            }
        }

        PlaybackStateCompat playbackStateCompat = playbackStateBuilder.build();
        mMediaSession.setPlaybackState(playbackStateCompat);

        if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING ||
                playbackStateCompat.getState() == PlaybackStateCompat.STATE_PAUSED) {
            runPlaybackState();
        } else {
            stopPlaybackState();
        }

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

        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                mediaPlayer.getDuration() / 1000);

        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);


        mMediaSession.setMetadata(builder.build());
    }


    private Handler playbackStateHandler;
    private Runnable playbackStateRunnable;


    private void runPlaybackState() {
        playbackStateHandler = new Handler();
        if (playbackStateRunnable == null) {
            playbackStateRunnable = new Runnable() {
                @Override
                public void run() {

                    updatePlaybackState();

                    playbackStateHandler.postDelayed(this, 1000);
                }
            };
            playbackStateRunnable.run();
        }
    }

    private void stopPlaybackState() {
        playbackStateHandler.removeCallbacks(playbackStateRunnable);
        playbackStateRunnable = null;
    }


    @Override
    public BrowserRoot onGetRoot(String clientPackageName,
                                 int clientUid, Bundle rootHints) {
        return new BrowserRoot(
                getString(R.string.app_name),
                null
        );
    }

    @Override
    public void onLoadChildren(String parentId,
                               Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
    }
}

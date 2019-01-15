package com.khahani.app.audioandvideosample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String LOG_TAG =
            MediaPlaybackService.class.getSimpleName();
    public static final String ACTION_UPDATE_META_DATA = "action_update_meta_data";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "media_playback_channel";

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

                if (mediaPlayer == null){
                    initialMediaPlayer();
                }else{
                    play();
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                mediaPlayer.pause();
                updatePlaybackState();
                stopForeground(false);
            }

            @Override
            public void onStop() {
                super.onStop();
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                updatePlaybackState();

                stopForeground(true);
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
                mediaPlayer.seekTo((int) position);
                updatePlaybackState();
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

            startForeground(NOTIFICATION_ID, buildMediaNotification());
        }
    }

    private Notification buildMediaNotification() {

        // You only need to create the channel on API 26+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        MediaControllerCompat controller = mMediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID);

        builder
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(MediaButtonReceiver
                        .buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        builder
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        builder
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_shape_pause,
                        getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this, PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_shape_next,
                        getString(R.string.skip_to_next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));

        builder
                .setStyle(new MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mMediaSession.getSessionToken())
//These two lines are only required if your minSdkVersion is <API 21
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this, PlaybackStateCompat.ACTION_STOP)));

        builder.setOnlyAlertOnce(true);

        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mMediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
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
            mediaPlayer.setDataSource(getString(R.string.audio_link));
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


    public void updateMetadata() {

        MediaMetadataCompat.Builder builder =
                new MediaMetadataCompat.Builder();

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        if (Build.VERSION.SDK_INT >= 14)
            mmr.setDataSource(getString(R.string.audio_link), new HashMap<String, String>());
        else
            mmr.setDataSource(getString(R.string.audio_link));

        String album =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String description = String.format("%s %s",
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR),
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));
        byte[] image = mmr.getEmbeddedPicture();

        if (image != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    bitmap);
        }

        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                mediaPlayer.getDuration() / 1000);

        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, album);
        builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist);
        builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, description);


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

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager
                mNotificationManager =
                (NotificationManager) this
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = CHANNEL_ID;
        // The user-visible name of the channel.
        CharSequence name = "Media playback";
        // The user-visible description of the channel.
        String description = "Media playback controls";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
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

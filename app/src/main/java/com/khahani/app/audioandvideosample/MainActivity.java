package com.khahani.app.audioandvideosample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    private MediaPlayer.OnPreparedListener myOnPreparedListener =
            new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

                    int result = audioManager.requestAudioFocus(focusChangeListener,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){

                        registerNoisyReceiver();
                        mediaPlayer.start();

                    }

                }
            };

    private MediaPlayer.OnCompletionListener completionListener =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    audioManager.abandonAudioFocus(focusChangeListener);
                }
            };

    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {

                    AudioManager am =
                            (AudioManager)getSystemService(Context.AUDIO_SERVICE);

                    switch (focusChange) {
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) :
                            mediaPlayer.setVolume(0.2f, 0.2f);
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                            pauseAudioPlayback();
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS) :
                            mediaPlayer.stop();
                            am.abandonAudioFocus(this);
                            break;
                        case (AudioManager.AUDIOFOCUS_GAIN) :
                            mediaPlayer.setVolume(1f, 1f);
                            mediaPlayer.start();
                            break;
                        default: break;
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

        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource("http://sv.blogmusic.ir/myahang/Classic-music-1.mp3");
            mediaPlayer.setOnPreparedListener(myOnPreparedListener);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNoisyReceiver();
    }
}

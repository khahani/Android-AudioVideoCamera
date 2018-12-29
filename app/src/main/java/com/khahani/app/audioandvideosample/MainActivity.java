package com.khahani.app.audioandvideosample;

import android.content.Context;
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
                            mediaPlayer.pause();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource("http://sv.blogmusic.ir/myahang/Classic-music-1.mp3");
            mediaPlayer.setOnPreparedListener(myOnPreparedListener);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

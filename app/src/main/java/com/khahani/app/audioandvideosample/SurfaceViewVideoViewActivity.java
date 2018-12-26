package com.khahani.app.audioandvideosample;

import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.IOException;
import android.view.View.OnClickListener;

public class SurfaceViewVideoViewActivity extends AppCompatActivity
        implements SurfaceHolder.Callback {

    static final String TAG = "VideoViewActivity";

    private MediaPlayer mediaPlayer;

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {

            mediaPlayer.setDisplay(surfaceHolder);
            File file = new File(Environment.getExternalStorageDirectory(),
                    "dont travel to iran.mp4");
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument Exception", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Illegal State Exception", e);
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception", e);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception", e);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mediaPlayer.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder,
                               int format, int width, int height) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view_video_view);

        mediaPlayer = new MediaPlayer();

        final SurfaceView surfaceView =
                findViewById(R.id.surfaceView);
        // Configure the Surface View.
        surfaceView.setKeepScreenOn(true);
        // Configure the Surface Holder and register the callback.
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setFixedSize(400, 300);

        Button playButton = findViewById(R.id.buttonPlay);
        playButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });

        Button pauseButton = findViewById(R.id.buttonPause);
        pauseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mediaPlayer.pause();
            }
        });

        Button skipButton = findViewById(R.id.buttonSkip);
        skipButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mediaPlayer.seekTo(mediaPlayer.getDuration()/2);
            }
        });
    }
}

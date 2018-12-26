package com.khahani.app.audioandvideosample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
        implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

    static final String TAG = "VideoViewActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private MediaPlayer mediaPlayer;

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {

            mediaPlayer.setDisplay(surfaceHolder);
            File file = new File(Environment.getExternalStorageDirectory(),
                    "dont travel to iran.mp4");
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();

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

        disableButtons();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // explain why need permissions
                showExplanation();

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            getReady();
        }

    }

    private void disableButtons() {
        Button start = findViewById(R.id.buttonPlay);
        Button pause = findViewById(R.id.buttonPause);
        Button skip = findViewById(R.id.buttonSkip);

        start.setEnabled(false);
        pause.setEnabled(false);
        skip.setEnabled(false);
    }

    private void enableButtons() {
        Button start = findViewById(R.id.buttonPlay);
        Button pause = findViewById(R.id.buttonPause);
        Button skip = findViewById(R.id.buttonSkip);

        start.setEnabled(true);
        pause.setEnabled(true);
        skip.setEnabled(true);
    }

    private void showExplanation() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.request_permission_title)
                .setMessage(R.string.request_permision_read_external_storage_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(SurfaceViewVideoViewActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                })
                .create();
        dialog.show();
    }

    private void getReady() {
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
                mediaPlayer.seekTo(mediaPlayer.getDuration() / 2);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    finish();
                    startActivity(getIntent());
                } else {
                    // permission denied, boo!
                    finish();
                }
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        enableButtons();
        //mediaPlayer.start();
    }
}

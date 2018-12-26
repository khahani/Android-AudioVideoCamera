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

//todo: 8. implement SurfaceHolder.Callback for your Activity and implement it's methods
public class SurfaceViewVideoViewActivity extends AppCompatActivity
        {

            //todo: 1. define String field and call it TAG and set its value to "VideoViewActivity"

            //todo: 2. define field MediaPlayer call it mediaPlayer

            //todo: 9. cut todos number 10 to 15 into the surfaceCreated method and continue
            //todo: 10. call mediaPlayer.setDisplay method and pass the surfaceHolder parameter to it
            //todo: 11. Read a file from external storage and save it to a File object and call it file
            //todo: 12. call mediaPlayer.setDataSource and pass it file.getPath method
            //todo: 13. call mediaPlayer.prepare method
            //todo: 14. suround all code from todo 10 to 13  into a try-catch block
            //todo: 15. add IOException for catch block


            //todo: 16. call mediaPlayer.release on surfaceDestroyed method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view_video_view);


        //todo: 3. initialize mediaPlayer by calling its default constructor

        //todo: 5. create a final SurfaceView object and call it surfaceView and connect it
        // to surfaceView in your layout

        //todo: 6. Configure the Surface View by calling setKeepScreenOn method with true value.

        //todo: 7. Create an object of SurfaceHolder class and name it holder, initial it by
        // calling getHolder method from surfaceView object.

        //todo: 17. call holder.addCallback and pass it this as parameter

        //todo: 18. call holder.setFixedSize and set width = 400 and height = 300

        //todo: 19. create an object on Button class and call it playButton and connect to buttonPlay
        // in your layout by calling findViewById

        //todo: 20. set an OnClickListener to the button by calling setOnClickListener

        //todo: 21. call mediaPlayer.start method to playing the media.

        //todo: 22. create an object on Button class and call it pauseButton and connect to buttonPause
        // in your layout by calling findViewById

        //todo: 23. set an OnClickListener to the button by calling setOnClickListener

        //todo: 24. call mediaPlayer.pause method to pausing the playing media.

        //todo: 22. create an object on Button class and call it skipButton and connect to buttonSkip
        // in your layout by calling findViewById

        //todo: 23. set an OnClickListener to the button by calling setOnClickListener

        //todo: 24. call mediaPlayer.seekTo method to jump to center of media and for that
        // pass mediaPlayer.getDuration() / 2 as parameter to it.

    }
}

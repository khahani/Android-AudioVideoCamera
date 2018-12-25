package com.khahani.app.audioandvideosample;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //todo: 4. Define MediaPlayer.OnPreparedListener and call it myOnPreparedListener

    //todo: 5. inside the onPrepared method of the listener
    // call mediaPlayer.start method by using mediaPlayer parameter.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //todo: 2. Create an instance of MediaPlayer and call it mediaPlayer

        //todo: 3. Call setDataSource method by your music resource. If there is need to use
        //          try-catch do it and take care rest of your code.

        //todo: 6. Call onPreparedListener and

        //todo: 7. Call prepareAsync method to prepare media off the UI thread
    }
}

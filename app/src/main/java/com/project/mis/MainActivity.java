package com.project.mis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elmeyer.backhand.Backhand;
import com.elmeyer.backhand.Swipe;
import com.elmeyer.backhand.Tap;

import java.util.concurrent.TimeUnit;

/*
 * minimal music player based on tutorial :
 *      https://www.tutorialspoint.com/android/android_mediaplayer.htm
 */

public class MainActivity extends AppCompatActivity implements Backhand.OnSwipeListener {
    private static final String TAG = "MIS";

    private static final int CAMERA_PERMISSION = 65535;

    private SeekBar seekbar;
    private MediaPlayer mediaPlayer;
    public TextView songName, duration;
    private int [] songList  =  new int [3];
    private int [] song_thumbnail = new int [3];
    private Handler handler = new Handler();
    private double startTime = 0, finalTime = 0;
    private int forwardTime = 5000, backwardTime = 5000;
    private int skip = 1;
    ImageView imageView;

    boolean play_tip = false;
    boolean scrub_tip = false;
    boolean skip_tip = false;


    private static Backhand backhand;

    private static HandlerThread handlerThread = new HandlerThread("FpsHandlerThread");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // populate song list
        songList[0] =  R.raw.song_1;
        songList[1] =  R.raw.song_2;
        songList[2] =  R.raw.song_3;

        //song thumbnails
        song_thumbnail[0] =  R.drawable.thumb_1;
        song_thumbnail[1] =  R.drawable.thumb_2;
        song_thumbnail[2] =  R.drawable.thumb_3;

        //init view
        initializeViews(skip);

        // initialize Handler for background tasks such as FPS counting
        handlerThread.start();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run()
            {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION);
                } else {
                    try {
                        backhand = new Backhand(MainActivity.this,
                                MainActivity.this.getSystemService(CameraManager.class),
                                handlerThread.getLooper());
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Unable to instantiate Backhand!");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case CAMERA_PERMISSION: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        backhand = new Backhand(this, this.getSystemService(CameraManager.class),
                                                handlerThread.getLooper());
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Unable to instantiate Backhand!");
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Unable to instantiate Backhand! Reason: Camera permission not granted");
                }
                //return; void return signature
            }
        }
    }
    
    public void initializeViews(int song){
        imageView = (ImageView) findViewById(R.id.mp3Image);
        imageView.setImageResource(song_thumbnail[song]);

        mediaPlayer = MediaPlayer.create(this, songList[song]);
        duration = (TextView) findViewById(R.id.songDuration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        finalTime = mediaPlayer.getDuration();
        seekbar.setMax((int) finalTime);
        seekbar.setClickable(false);
    }

    // seekBar - handle
    private Runnable updateSeekBarTime = new Runnable() {
        @SuppressLint("DefaultLocale")
        public void run() {
            startTime = mediaPlayer.getCurrentPosition(); //get current position
            seekbar.setProgress((int) startTime);         //set seek-bar progress
            double timeRemaining = finalTime - startTime; //set time remaining
            duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
            handler.postDelayed(this, 100); //repeat evert 100 milliseconds
        }
    };

    // play
    public void play(View view) {
        if(!play_tip){
            Toast.makeText(getApplicationContext(), "TIP: you can double tap camera to play or pause! ", Toast.LENGTH_LONG).show();
            play_tip = true;
        }
        mediaPlayer.start();
        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        seekbar.setProgress((int) startTime);
        handler.postDelayed(updateSeekBarTime, 100);
    }

    // media player pause
    public void pause(View view) {
        if(!play_tip){
            Toast.makeText(getApplicationContext(), "TIP: you can double tap camera to play or pause! ", Toast.LENGTH_LONG).show();
            play_tip = true;
        }
        mediaPlayer.pause();
    }

    // media player recursive skip forward
    public void skipForward(View view) {
        if(!skip_tip){
            Toast.makeText(getApplicationContext(), "TIP: you can swipe up/down on camera to skip sound tracks! ", Toast.LENGTH_LONG).show();
            skip_tip = true;
        }
        mediaPlayer.stop();
        if(skip == 2){
            skip = 0;
        }
        else{
            skip++;
        }
        initializeViews(skip);
        play(null);
    }

    // media player recursive skip backward
    public void skipBackward(View view) {
        if(!skip_tip){
            Toast.makeText(getApplicationContext(), "TIP: you can swipe up/down on camera to skip sound tracks! ", Toast.LENGTH_LONG).show();
            skip_tip = true;
        }
        mediaPlayer.stop();
        if(skip == 0){
            skip = 2;
        }
        else{
            skip--;
        }
        initializeViews(skip);
        play(null);
    }


    // media player forward
    public void forward(View view) {
        if(!scrub_tip){
            Toast.makeText(getApplicationContext(), "TIP: you can triple tap camera to scrub through a sound track! ", Toast.LENGTH_LONG).show();
            scrub_tip = true;
        }
        int temp = (int)startTime;
        if((temp+forwardTime)<=finalTime){
            startTime = startTime + forwardTime;
            mediaPlayer.seekTo((int) startTime);
        }
    }

    // media player rewind
    public void rewind(View view) {
        if(!scrub_tip){
            Toast.makeText(getApplicationContext(), "TIP: you can triple tap camera to scrub through a sound track! ", Toast.LENGTH_LONG).show();
            scrub_tip = true;
        }
        int temp = (int)startTime;
        if((temp-backwardTime)>0){
            startTime = startTime - backwardTime;
            mediaPlayer.seekTo((int) startTime);
        }
    }

    @Override
    public void onSwipe(Swipe swipe) {
        // do nothing
    }

    @Override
    public void onTap(Tap tap) {
        if (tap == Tap.SINGLE) {
            if (mediaPlayer.isPlaying()) {
                pause(null);
            } else {
                play(null);
            }
        } else if (tap == Tap.DOUBLE) {
            forward(null);
        } else if (tap == Tap.TRIPLE) {
            rewind(null);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.pause();
    }


    @Override
    public void onPause() {
        //backhand.onPause(); todo : Attempt to invoke virtual method 'void com.elmeyer.backhand.Backhand.onPause()' on a null object reference
        super.onPause();
    }

    @Override
    public void onResume() {
        if (backhand != null) {
            try {
                backhand.onResume();
            } catch (CameraAccessException e) {
                Log.e(TAG, "Unable to access camera after resuming");
                e.printStackTrace();
            }
        }
        super.onResume();
    }
}

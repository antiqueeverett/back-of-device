package com.project.mis;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/*
 * minimal music player based on tutorial :
 *      https://www.tutorialspoint.com/android/android_mediaplayer.htm
 */

public class MainActivity extends AppCompatActivity{

    private SeekBar seekbar;
    private MediaPlayer mediaPlayer;
    public TextView songName, duration;
    private Handler handler = new Handler();
    private double startTime = 0, finalTime = 0;
    private int forwardTime = 5000, backwardTime = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
    }

    public void initializeViews(){
        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        duration = (TextView) findViewById(R.id.songDuration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        finalTime = mediaPlayer.getDuration();
        seekbar.setMax((int) finalTime);
        seekbar.setClickable(false);
    }

    // seekBar - handle
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition(); //get current position
            seekbar.setProgress((int) startTime);         //set seekbar progress
            double timeRemaining = finalTime - startTime; //set time remaining
            duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
            handler.postDelayed(this, 100); //repeat evert 100 milliseconds
        }
    };

    // play
    public void play(View view) {
        mediaPlayer.start();
        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        seekbar.setProgress((int) startTime);
        handler.postDelayed(updateSeekBarTime, 100);
    }

    // pause
    public void pause(View view) {
        mediaPlayer.pause();
    }

    // forward
    public void forward(View view) {
        int temp = (int)startTime;
        if((temp+forwardTime)<=finalTime){
            startTime = startTime + forwardTime;
            mediaPlayer.seekTo((int) startTime);
        }
    }

    // rewind
    public void rewind(View view) {
        int temp = (int)startTime;
        if((temp-backwardTime)>0){
            startTime = startTime - backwardTime;
            mediaPlayer.seekTo((int) startTime);
        }
    }
}
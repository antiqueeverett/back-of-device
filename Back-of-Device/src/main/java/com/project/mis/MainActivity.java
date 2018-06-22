package com.project.mis;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/*
 * minimal music player based on tutorial :
 *      https://www.tutorialspoint.com/android/android_mediaplayer.htm
 */

public class MainActivity extends AppCompatActivity{

    public static int init = 0;
    private double startTime = 0;
    private double finalTime = 0;
    private int forwardTime = 5000;
    private int backwardTime = 5000;

    private SeekBar seekbar;
    private ImageView thumbnail;
    private TextView progress_sec, duration, title;
    private Button scrub_forward, pause, play, scrub_back;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = (Button)findViewById(R.id.button_Play);
        pause = (Button) findViewById(R.id.button_Pause);
        thumbnail = (ImageView)findViewById(R.id.song_thumbnail);
        progress_sec = (TextView)findViewById(R.id.song_progress);
        scrub_back = (Button)findViewById(R.id.button_scrub_back);
        scrub_forward = (Button) findViewById(R.id.button_scrub_forward);

        //duration ?? = (TextView)findViewById(R.id.song_duration); //to add duration textview
        //title ?? = (TextView)findViewById(R.id.song_title);

        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        seekbar = (SeekBar)findViewById(R.id.song_seekBar);
        seekbar.setClickable(false);
        pause.setEnabled(false);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                finalTime = mediaPlayer.getDuration();
                startTime = mediaPlayer.getCurrentPosition();

                if(init == 0) {
                    seekbar.setMax((int) finalTime);
                    init = 1;
                }

                //duration.setText(String.format("%d min, %d sec",
                //        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                //        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                //                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                //                        finalTime)))
                //);

                progress_sec.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        startTime)))
                );

                seekbar.setProgress((int)startTime);
                handler.postDelayed(UpdateSongTime,100);
                pause.setEnabled(true);
                play.setEnabled(false);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                pause.setEnabled(false);
                play.setEnabled(true);
            }
        });

        scrub_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;
                if((temp+forwardTime)<=finalTime){
                    startTime = startTime + forwardTime;
                    mediaPlayer.seekTo((int) startTime);
                }
            }
        });

        scrub_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;
                if((temp-backwardTime)>0){
                    startTime = startTime - backwardTime;
                    mediaPlayer.seekTo((int) startTime);
                }
            }
        });
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            progress_sec.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );

            seekbar.setProgress((int)startTime);
            handler.postDelayed(this, 100);
        }
    };
}

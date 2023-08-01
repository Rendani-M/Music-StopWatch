package com.musicplayerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private Button testButton, backButton;

    private MediaPlayer mediaPlayer;
    private ScheduledExecutorService timer;
    private ArrayList<MusicModel>  music;

    public static final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE= "STORED_MUSIC";
    public static boolean SHOW_MINI_PLAYER= false;
    public static String PATH_TO_FRAG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        testButton= findViewById(R.id.test_button);
        backButton= findViewById(R.id.back_button);
        music= new ArrayList<>();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TimeActivity.this, MainActivity.class));
            }
        });
    }

    public void createMediaPlayer(Uri uri){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                   releaseMediaPlayer();
                }
            });

        } catch (IOException e){

        }
    }

    private void releaseMediaPlayer() {
        if (timer != null) {
            timer.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }



    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences= getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
        Gson gson= new Gson();
        Type type= new TypeToken<ArrayList<MusicModel>>(){}.getType();
        String value= preferences.getString(MUSIC_FILE, null);
        music= gson.fromJson(value, type);

        if(music == null)
            music= new ArrayList<>();

        if(value != null){
            Toast.makeText(TimeActivity.this, "Size: "+String.valueOf(music.size()),Toast.LENGTH_LONG).show();
            SHOW_MINI_PLAYER= true;
            PATH_TO_FRAG= value;
            //createMediaPlayer(Uri.parse(PATH_TO_FRAG));
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    //mediaPlayer.pause();
                    Toast.makeText(TimeActivity.this, "Playing",Toast.LENGTH_LONG).show();
                    testButton.setText("PAUSE");
                    //timer.shutdown();
                } else {
                    testButton.setText("PLAY");
                }
            }

            testButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //createMediaPlayer(Uri.parse(PATH_TO_FRAG));
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            testButton.setText("PLAY");
                            timer.shutdown();
                        } else {
                            //start=true;
                            mediaPlayer.start();
                            testButton.setText("PAUSE");

                            timer = Executors.newScheduledThreadPool(1);
                            timer.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    if (mediaPlayer != null) {
                                        //if (!seekbar1.isPressed()) {
                                           //seekbar1.setProgress(mediaPlayer.getCurrentPosition());
                                        //}
                                    }
                                }
                            }, 100, 100, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            });

        }
        else{
            Toast.makeText(TimeActivity.this, "Not Playing",Toast.LENGTH_LONG).show();
            SHOW_MINI_PLAYER= false;
            PATH_TO_FRAG= null;
        }
    }

}
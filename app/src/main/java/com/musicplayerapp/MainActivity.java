package com.musicplayerapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface{
    //Player Activity
    TextView textview2;
    TextView textview3;
    Button button1;
    Button button2;
    Button timeButton;
    SeekBar seekbar1;

    String duration;
    MediaPlayer mediaPlayer;
    ScheduledExecutorService timer;
    public static final int PICK_FILE =99;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<MusicModel>  music;

    private AudioAdapter adapter;

    private int pos=0;
    public static final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE= "STORED_MUSIC";
    public static final String MUSIC_PLAY= "PLAY_MUSIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        timeButton= findViewById(R.id.timeButton);
        textview2 = findViewById(R.id.textView2);
        textview3 = findViewById(R.id.textView3);
        seekbar1 = findViewById(R.id.seekbar1);

        music= new ArrayList<>();

        recyclerView= findViewById(R.id.recyclerview_main);
        layoutManager= new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        adapter= new AudioAdapter(music, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TimeActivity.class));
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent audio = new Intent();
                audio.setType("audio/*");
                audio.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                //audio.setAction(android.content.Intent.ACTION_VIEW);
                audio.setAction(audio.ACTION_GET_CONTENT);
                //audio.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(audio, "Select Audio"), PICK_FILE);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        //mediaPlayer.release();
                        button2.setText("PLAY");
                        timer.shutdown();
                    } else {
                        mediaPlayer.start();
                        button2.setText("PAUSE");
                        whileMusicIsPlaying();
                    }
                }
            }
        });

        seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null){
                    long millis = mediaPlayer.getCurrentPosition();
                    long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
                    long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
                    long secs = total_secs - (mins*60);
                    textview3.setText(mins + ":" + secs + " / " + duration);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekbar1.getProgress());
                }
            }
        });
        //button2.setEnabled(false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK){
            if(data.getClipData() != null){
                for(int i=0; i< data.getClipData().getItemCount(); i++){
                    Uri musicUri= data.getClipData().getItemAt(i).getUri();
                    String musicTitle= getNameFromUri(musicUri);
                    music.add(new MusicModel(musicUri, musicTitle));
                }

            }else{
                Uri musicUri= data.getData();
                String musicTitle= getNameFromUri(musicUri);
                music.add(new MusicModel(musicUri, musicTitle));
            }
            Toast.makeText(MainActivity.this, "Size: "+ String.valueOf(music.size()),Toast.LENGTH_LONG).show();
            adapter.notifyDataSetChanged();
        }

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

            textview2.setText(getNameFromUri(uri));
            button2.setEnabled(true);

            int millis = mediaPlayer.getDuration();
            long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
            long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
            long secs = total_secs - (mins*60);
            duration = mins + ":" + secs;
            textview3.setText("00:00 / " + duration);
            seekbar1.setMax(millis);
            seekbar1.setProgress(0);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(pos< music.size()-1){
                        pos++;
                        releaseMediaPlayer();
                        createMediaPlayer(music.get(pos).getUri());
                        musicPlaying();
                    }else{
                        releaseMediaPlayer();
                    }
                    //Toast.makeText(MainActivity.this, "Position: "+ String.valueOf(pos),Toast.LENGTH_LONG).show();

                }
            });

        } catch (IOException e){
            textview2.setText(e.toString());
        }
    }

    public void musicPlaying(){
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                button2.setText("PLAY");
                timer.shutdown();
            } else {
                //start=true;
                mediaPlayer.start();
                button2.setText("PAUSE");

                whileMusicIsPlaying();
            }
        }
    }

    @SuppressLint("Range")
    public String getNameFromUri(Uri uri){
        String fileName = "";
        Cursor cursor = null;
        cursor = getContentResolver().query(uri, new String[]{
                MediaStore.Images.ImageColumns.DISPLAY_NAME
        }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
        }
        if (cursor != null) {
            cursor.close();
        }
        return fileName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    public void releaseMediaPlayer(){
        if (timer != null) {
            timer.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        button2.setEnabled(false);
        textview2.setText("TITLE");
        textview3.setText("00:00 / 00:00");
        seekbar1.setMax(100);
        seekbar1.setProgress(0);
    }

    public void whileMusicIsPlaying(){
        timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    if (!seekbar1.isPressed()) {
                        seekbar1.setProgress(mediaPlayer.getCurrentPosition());
                    }
                }
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onItemClick(int position, boolean delete) {
        pos=position;

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                //button2.setText("PLAY");
                timer.shutdown();
                whileMusicIsPlaying();
            } else {
                //start=true;
                pos=position;
                mediaPlayer.start();
                button2.setText("PAUSE");

                whileMusicIsPlaying();
            }
        }else{
            createMediaPlayer(music.get(position).getUri());
            mediaPlayer.start();
            button2.setText("PAUSE");
            whileMusicIsPlaying();
        }

        SharedPreferences.Editor editor= getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        Gson gson= new Gson();
        String m= gson.toJson(music);
        editor.putString(MUSIC_FILE, m);
        editor.apply();
    }


}
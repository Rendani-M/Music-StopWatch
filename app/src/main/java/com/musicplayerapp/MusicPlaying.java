package com.musicplayerapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MusicPlaying extends Service {

    private static MediaPlayer mediaPlayer;
    private static ScheduledExecutorService timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static MediaPlayer createMusicPlayer(Context context, Uri uri){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaPlayer;
    }
}

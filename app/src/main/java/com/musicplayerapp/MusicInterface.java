package com.musicplayerapp;

import android.media.MediaPlayer;

import java.util.concurrent.ScheduledExecutorService;

public interface MusicInterface {
    public void playingMusic(MediaPlayer mediaPlayer);
    public void playingTime(ScheduledExecutorService timer);
}

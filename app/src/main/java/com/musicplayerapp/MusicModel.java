package com.musicplayerapp;

import android.net.Uri;

public class MusicModel {
    private Uri uri;
    private String title;

    public MusicModel(Uri uri, String title) {
        this.uri = uri;
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

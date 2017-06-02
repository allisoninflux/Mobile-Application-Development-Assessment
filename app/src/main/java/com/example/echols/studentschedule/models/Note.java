package com.example.echols.studentschedule.models;

import android.net.Uri;
import android.support.annotation.NonNull;

public class Note {

    private final Uri uri;
    public static final int TEXT = 0, IMAGE = 1;

    public Note(@NonNull Uri uri) {
        this.uri = uri;
    }

    public String getFileName() {
        return uri.getLastPathSegment();
    }

    public Uri getUri() {
        return uri;
    }

    public int getType() {

        String path = uri.getPath();

        String ext = path.substring(path.length() - 3);
        if(ext.equals("txt")) {
            return TEXT;
        } else if(ext.equals("jpg")) {
            return IMAGE;
        }
        return -1;
    }

    @Override
    public String toString() {
        return getFileName();
    }
}

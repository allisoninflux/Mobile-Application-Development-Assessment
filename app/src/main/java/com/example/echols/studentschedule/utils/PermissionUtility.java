package com.example.echols.studentschedule.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.example.echols.studentschedule.activities.MainActivity;

/**
 * A simple utility to get (or request) external write permission
 */
public class PermissionUtility {

    private final static int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    private final Context context;

    public PermissionUtility(Context context, OnPermissionGrantedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void getWriteExternalStoragePermission() {
        if (!MainActivity.HAS_CAMERA) {
            return;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity)context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            listener.onPermissionGranted();
        }
    }

    private final OnPermissionGrantedListener listener;

    public interface OnPermissionGrantedListener {
        void onPermissionGranted();
    }

}

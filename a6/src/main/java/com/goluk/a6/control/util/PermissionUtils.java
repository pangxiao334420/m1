package com.goluk.a6.control.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.helper.PermissionHelper;

/**
 * Permissionutil -- EasyPermission
 */
public class PermissionUtils {
    public static final int CODE_REQUEST_CAMERA_PERMISSION = 8001;
    public static final int CODE_REQUEST_LOCATION_PERMISSION = 8002;
    public static final int CODE_REQUEST_STORAGE_PERMISSION = 8003;
    public static final int CODE_REQUEST_SETTING_STORAGE_PERMISSION = 8004;

    public static boolean hasStoragePermission(Context context) {
        return EasyPermissions.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean hasLocationgPermission(Context context) {
        return EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean hasCameraPermission(Context context) {
        return EasyPermissions.hasPermissions(context, Manifest.permission.CAMERA);
    }

    public static void requestLocationAndStoragePermission(Object target) {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        requestPermission(target, CODE_REQUEST_LOCATION_PERMISSION, permissions);
    }

    public static void requestLocationPermission(Object target) {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        requestPermission(target, CODE_REQUEST_LOCATION_PERMISSION, permissions);
    }

    public static void requestCameraPermission(Object target) {
        String[] permissions = new String[]{Manifest.permission.CAMERA};

        requestPermission(target, CODE_REQUEST_CAMERA_PERMISSION, permissions);
    }

    @SuppressLint("RestrictedApi")
    private static void requestPermission(Object target, int requestCode, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        PermissionHelper permissionHelper = null;

        if (target instanceof Activity) {
            permissionHelper = PermissionHelper.newInstance((Activity) target);
        } else if (target instanceof Fragment) {
            permissionHelper = PermissionHelper.newInstance((Fragment) target);
        } else if (target instanceof android.app.Fragment) {
            permissionHelper = PermissionHelper.newInstance((android.app.Fragment) target);
        }

        if (permissionHelper != null)
            permissionHelper.directRequestPermissions(requestCode, permissions);
    }

}

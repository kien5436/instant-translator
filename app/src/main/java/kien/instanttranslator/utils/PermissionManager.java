package kien.instanttranslator.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionManager {

  public static final int REQUEST_PERMISSION = 1;
  private static final String[] PERMISSIONS = {
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.CAMERA,
      Manifest.permission.INTERNET
  };

  public static void askPermissions(Context context) {

    ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, REQUEST_PERMISSION);
  }

  public static boolean hasPermissions(Context context) {

    if ( null != context && null != PERMISSIONS )
      for (String perm : PERMISSIONS)
        if ( PackageManager.PERMISSION_GRANTED != ActivityCompat
            .checkSelfPermission(context, perm) )
          return false;

    return true;
  }
}

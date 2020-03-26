package kien.instanttranslator.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import kien.instanttranslator.R;
import kien.instanttranslator.services.FloatingWidgetService;
import kien.instanttranslator.utils.PermissionManager;

public class MainActivity extends AppCompatActivity {

  private final String TAG = getClass().getSimpleName();

  private BroadcastReceiver broadcastReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    checkPermissions();

    createWidget();

    takeScreenshot(0, 0, 100, 100);

  }

  private void checkPermissions() {

    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this) ) {

      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, PermissionManager.REQUEST_PERMISSION);
    }

    if ( !PermissionManager.hasPermissions(this) )
      PermissionManager.askPermissions(this);
  }

  private void createWidget() {

    startService(new Intent(MainActivity.this, FloatingWidgetService.class));
    finish();
  }

  public void takeScreenshot(int x, int y, int width, int height) {

    Date now = new Date();
    android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

    try {
      // image naming and path  to include sd card  appending name you choose for file
      String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

      // create bitmap screen capture
      View v1 = getWindow().getDecorView().getRootView();
      v1.setDrawingCacheEnabled(true);
      Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache(), x, y, width, height);
      v1.setDrawingCacheEnabled(false);

      File imageFile = new File(mPath);

      FileOutputStream outputStream = new FileOutputStream(imageFile);
      int quality = 100;
      bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
      outputStream.flush();
      outputStream.close();
      openScreenshot(imageFile);
    }
    catch (Throwable e) {
      Log.e(TAG, "takeScreenshot: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void openScreenshot(File imageFile) {

    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_VIEW);
    Uri uri = Uri.fromFile(imageFile);
    intent.setDataAndType(uri, "image/*");
    startActivity(intent);
  }
}

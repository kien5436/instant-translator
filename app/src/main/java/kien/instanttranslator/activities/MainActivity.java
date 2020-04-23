package kien.instanttranslator.activities;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import kien.instanttranslator.R;
import kien.instanttranslator.services.FloatingWidgetService;
import kien.instanttranslator.translation.Translator;
import kien.instanttranslator.utils.PermissionManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private final String TAG = getClass().getSimpleName();
  private final int REQUEST_SCREENSHOT = 5436;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    checkPermissions();

    bindView();
  }

  private void bindView() {

    findViewById(R.id.btnStart).setOnClickListener(this);
    findViewById(R.id.btnManage).setOnClickListener(this);
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

  @Override
  public void onClick(View v) {

    switch (v.getId()) {
      case R.id.btnStart:
        createWidget();
        break;
      case R.id.btnManage:
        Toast.makeText(this, "Developing...", Toast.LENGTH_SHORT).show();
//        new Thread(new Runnable() {
//
//          @Override
//          public void run() {
//
//            Translator translator = new Translator();
//
//            translator.setOriginalText("hello");
//           String text = translator.translate();
//            Log.d(TAG, "run: "+text);
//          }
//        }).start();
        break;
    }
  }

  private void createWidget() {

    MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

    if ( null != mediaProjectionManager )
      startActivityForResult(mediaProjectionManager
          .createScreenCaptureIntent(), REQUEST_SCREENSHOT);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    if ( REQUEST_SCREENSHOT == requestCode && RESULT_OK == resultCode ) {

      Intent intent = new Intent(MainActivity.this, FloatingWidgetService.class)
          .putExtra(FloatingWidgetService.EXTRA_RESULT_CODE, resultCode)
          .putExtra(FloatingWidgetService.EXTRA_RESULT_INTENT, data);

      startService(intent);
      finish();
    }
  }
}

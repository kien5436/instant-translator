package kien.instanttranslator.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import kien.instanttranslator.R;
import kien.instanttranslator.activities.MainActivity;
import kien.instanttranslator.utils.Screenshot;

public class FloatingWidgetService extends Service implements View.OnClickListener {

  private final String TAG = getClass().getSimpleName();
  private final HandlerThread handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);

  public static final String EXTRA_RESULT_CODE = "RESULT_CODE";
  public static final String EXTRA_RESULT_INTENT = "RESULT_INTENT";
  private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

  private WindowManager windowManager;
  private View floatingView;
  private View collapsedView;
  private View expandedView;
  private WindowManager.LayoutParams params;
  private MediaProjection mediaProjection;
  private MediaProjectionManager mediaProjectionManager;
  private VirtualDisplay virtualDisplay;
  private Handler handler;
  private int resultCode;
  private Intent resultData;

  public Handler getHandler() { return this.handler; }

  public WindowManager getWindowManager() { return this.windowManager; }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) { return null; }

  @Override
  public void onCreate() {

    // creating floating widget and adding to window service
    floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null);
    params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    );
    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

    if ( null != windowManager )
      windowManager.addView(floatingView, params);

    bindView();

    // init mediaProjectionManager and thread to capture screen
    mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    if ( null == intent.getAction() ) {

      resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_OK);
      resultData = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
    }

    return super.onStartCommand(intent, flags, startId);
  }

  // binding components to the main view
  private void bindView() {

    collapsedView = floatingView.findViewById(R.id.layoutCollapsed);
    expandedView = floatingView.findViewById(R.id.layoutExpanded);

    floatingView.findViewById(R.id.ivClose).setOnClickListener(this);
    expandedView.setOnClickListener(this);

    floatingView.findViewById(R.id.layoutParent).setOnTouchListener(new View.OnTouchListener() {

      private int initialX;
      private int initialY;
      private float initialTouchX;
      private float initialTouchY;

      @SuppressLint("ClickableViewAccessibility")
      @Override
      public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            initialX = params.x;
            initialY = params.y;
            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();
            return true;
          case MotionEvent.ACTION_UP:
            // when the drag is ended switching the state of the widget
//            collapsedView.setVisibility(View.GONE);
//            expandedView.setVisibility(View.VISIBLE);
            params.x = initialX + (int) (event.getRawX() - initialTouchX);
            params.y = initialY + (int) (event.getRawY() - initialTouchY);
            Log.d(TAG, "onTouch: takeScreenshot " + initialX + " " + initialY + " " + params.x + " " + params.y);
            if ( null != resultData ) startCapture();
            return true;
          case MotionEvent.ACTION_MOVE:
            // this code is helping the widget to move around the screen with fingers
            params.x = initialX + (int) (event.getRawX() - initialTouchX);
            params.y = initialY + (int) (event.getRawY() - initialTouchY);
            windowManager.updateViewLayout(floatingView, params);
            return true;
        }

        return false;
      }
    });
  }

  @Override
  public void onClick(View v) {

    switch (v.getId()) {
      case R.id.ivClose:
        stopSelf();

        Intent intent = new Intent(FloatingWidgetService.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        break;
      case R.id.layoutExpanded:
//        collapsedView.setVisibility(View.VISIBLE);
//        expandedView.setVisibility(View.GONE);
        break;
    }
  }

  @Override
  public void onDestroy() {

    stopCapture();

    if ( null != floatingView ) windowManager.removeView(floatingView);
  }

  private void startCapture() {

    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
    final Screenshot screenshot = Screenshot.getInstance(FloatingWidgetService.this);
    MediaProjection.Callback cb = new MediaProjection.Callback() {

      @Override
      public void onStop() {

        virtualDisplay.release();
      }
    };
    virtualDisplay = mediaProjection
        .createVirtualDisplay("virtualDisplay",
            screenshot.getWidth(), screenshot.getHeight(),
            getResources().getDisplayMetrics().densityDpi,
            VIRTUAL_DISPLAY_FLAGS,
            screenshot.getSurface(), null, handler);

    mediaProjection.registerCallback(cb, handler);
  }

  public void stopCapture() {

    if ( null != mediaProjection ) {

      mediaProjection.stop();
      mediaProjection = null;
      virtualDisplay.release();
    }
  }
}

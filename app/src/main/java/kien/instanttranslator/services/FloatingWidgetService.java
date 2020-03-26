package kien.instanttranslator.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import kien.instanttranslator.R;

public class FloatingWidgetService extends Service implements View.OnClickListener {

  private final String TAG = getClass().getSimpleName();

  private WindowManager windowManager;
  private View floatingView;
  private View collapsedView;
  private View expandedView;
  private WindowManager.LayoutParams params;

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
        break;
      case R.id.layoutExpanded:
//        collapsedView.setVisibility(View.VISIBLE);
//        expandedView.setVisibility(View.GONE);
        break;
    }
  }

  @Override
  public void onDestroy() {

    if ( null != floatingView ) windowManager.removeView(floatingView);
  }
}

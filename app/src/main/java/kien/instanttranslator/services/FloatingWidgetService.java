package kien.instanttranslator.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;

import kien.instanttranslator.R;
import kien.instanttranslator.activities.MainActivity;
import kien.instanttranslator.utils.ScreenshotHandler;

public class FloatingWidgetService extends Service implements View.OnClickListener {

  private final String TAG = getClass().getSimpleName();

  public static final String EXTRA_RESULT_CODE = "RESULT_CODE";
  public static final String EXTRA_RESULT_INTENT = "RESULT_INTENT";
  public static final int UPDATE_UI_SHOW_WAITING_VIEW = 0;
  public static final int UPDATE_UI_SHOW_RESULT = 1;
  public static final int UPDATE_UI_SHOW_ERROR = 2;

  private static WindowManager windowManager;
  private View floatingView;
  private View collapsedView;
  private View expandedView;
  private MaterialTextView tvTranslated;
  private WindowManager.LayoutParams params;

  private ScreenshotHandler screenshotHandler;
  private int screenHeight;

  public static WindowManager getWindowManager() { return windowManager; }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) { return null; }

  @SuppressLint("InflateParams")
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

    DisplayMetrics displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    screenHeight = displayMetrics.heightPixels;

    bindView();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    if ( null == intent.getAction() ) {

      int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_OK);
      Intent resultData = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
      try {
        screenshotHandler = new ScreenshotHandler(this, resultCode, resultData);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    return super.onStartCommand(intent, flags, startId);
  }

  // binding components to the main view
  private void bindView() {

    collapsedView = floatingView.findViewById(R.id.layoutCollapsed);
    expandedView = floatingView.findViewById(R.id.layoutExpanded);
    tvTranslated = floatingView.findViewById(R.id.tvTranslated);

    floatingView.findViewById(R.id.ivClose).setOnClickListener(this);
    floatingView.findViewById(R.id.tvTranslated).setOnClickListener(this);

    collapsedView.setOnTouchListener(new View.OnTouchListener() {

      private int initialX;
      private int initialY;
      private float initialTouchX;
      private float initialTouchY;

      @SuppressLint("ClickableViewAccessibility")
      @Override
      public boolean onTouch(View v, final MotionEvent event) {

        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            initialX = params.x;
            initialY = params.y;
            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();

            expandedView.setVisibility(View.GONE);
            return true;
          case MotionEvent.ACTION_UP:
            // hiding collapsedView and take screenshots in background thread
            collapsedView.setVisibility(View.INVISIBLE);

            RelativeLayout.LayoutParams expandedViewParams = (RelativeLayout.LayoutParams) expandedView
                .getLayoutParams();

            if ( (float) (screenHeight / 2) < event.getRawY() )
              expandedViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            else expandedViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            expandedView.setLayoutParams(expandedViewParams);

            floatingView.post(new Runnable() {

              @Override
              public void run() {

                screenshotHandler.setTouchX(event.getRawX());
                screenshotHandler.setTouchY(event.getRawY());
                screenshotHandler.startCapture();
              }
            });
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
    }
  }

  @Override
  public void onDestroy() {

    if ( null != floatingView ) windowManager.removeView(floatingView);
    if ( null != screenshotHandler ) screenshotHandler.destroy();
  }

  /**
   * Update UI from background thread
   *
   * @param code       {@link FloatingWidgetService}'s constants
   * @param resultData Result from background. Passing <i>null</i> if not needed
   */
  public void updateUI(int code, final String resultData) {

    switch (code) {
      case UPDATE_UI_SHOW_WAITING_VIEW:
        floatingView.post(new Runnable() {

          @Override
          public void run() {

            collapsedView.setVisibility(View.VISIBLE);
            tvTranslated.setText(getResources().getString(R.string.wait));
            expandedView.setVisibility(View.VISIBLE);
          }
        });
        break;
      case UPDATE_UI_SHOW_RESULT:
        floatingView.post(new Runnable() {

          @Override
          public void run() { tvTranslated.setText(resultData); }
        });
        break;
      case UPDATE_UI_SHOW_ERROR:
        floatingView.post(new Runnable() {

          @Override
          public void run() {

            expandedView.setVisibility(View.GONE);
            collapsedView.setVisibility(View.VISIBLE);
          }
        });
        Toast.makeText(this, resultData, Toast.LENGTH_SHORT).show();
        break;
    }
  }
}
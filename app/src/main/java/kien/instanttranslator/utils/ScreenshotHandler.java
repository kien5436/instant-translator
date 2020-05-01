package kien.instanttranslator.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import kien.instanttranslator.R;
import kien.instanttranslator.ocr.MobileVisionAPI;
import kien.instanttranslator.services.FloatingWidgetService;
import kien.instanttranslator.translation.Translator;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

public class ScreenshotHandler {

  private final String TAG = getClass().getSimpleName();
  private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

  private HandlerThread handlerThread;
  private Handler handler;
  private MediaProjection mediaProjection;
  private MediaProjectionManager mediaProjectionManager;
  private VirtualDisplay virtualDisplay;
  private int resultCode;
  private Intent resultData;
  private Screenshot screenshot;
  private ImageReader imageReader;
  private boolean isCapturing = false;

  private Translator translator;
  private MobileVisionAPI mobileVisionAPI;

  private Context context;
  private float touchX;
  private float touchY;

  public ScreenshotHandler(Context context, int resultCode, Intent resultData) {

    this.context = context;
    this.resultCode = resultCode;
    this.resultData = resultData;

    // init MediaProjectionManager and thread to capture screen
    mediaProjectionManager = (MediaProjectionManager) context
        .getSystemService(MEDIA_PROJECTION_SERVICE);
    handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
    screenshot = Screenshot
        .getInstance(FloatingWidgetService.getWindowManager().getDefaultDisplay());
    imageReader = ImageReader
        .newInstance(screenshot.getWidth(), screenshot.getHeight(), PixelFormat.RGBA_8888, 2);

    translator = new Translator(context.getApplicationContext());
    mobileVisionAPI = new MobileVisionAPI(context.getApplicationContext());
  }

  public void setTouchX(float touchX) { this.touchX = touchX; }

  public void setTouchY(float touchY) { this.touchY = touchY; }

  public void startCapture() {

    Log.d(TAG, "startCapture: ");
    if ( null != mediaProjection || null == resultData )
      return; // hmm, I feel something is not good here

    isCapturing = true;
    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
    MediaProjection.Callback cb = new MediaProjection.Callback() {

      @Override
      public void onStop() { virtualDisplay.release(); }
    };
    virtualDisplay = mediaProjection
        .createVirtualDisplay("virtualDisplay",
            screenshot.getWidth(), screenshot.getHeight(),
            context.getResources().getDisplayMetrics().densityDpi,
            VIRTUAL_DISPLAY_FLAGS,
            imageReader.getSurface(), null, handler);

    mediaProjection.registerCallback(cb, handler);

    final ImageReader.OnImageAvailableListener imageAvailableListener = reader -> {

//        if ( !isCapturing ) return;

      FloatingWidgetService service = (FloatingWidgetService) context;

      Bitmap bitmap = screenshot.getImage(imageReader);
      stopCapture();
      service.updateUI(FloatingWidgetService.UPDATE_UI_SHOW_WAITING_VIEW, null);

      if ( null == bitmap ) return;

      // do OCR
      float x = touchX * Screenshot.SCALE_RATIO;
      float y = touchY * Screenshot.SCALE_RATIO;
      String extractedText = null;
      try {
        extractedText = mobileVisionAPI.extractText(bitmap, x, y);
      }
      catch (LowStorageException e) {
        service.updateUI(FloatingWidgetService.UPDATE_UI_SHOW_ERROR, e.getLocalizedMessage());
      }

      translator.setOriginalText(extractedText);
      try {
        String translated = translator.translate();
        service.updateUI(FloatingWidgetService.UPDATE_UI_SHOW_RESULT, translated);
      }
      catch (TimeoutException | InterruptedException | ExecutionException e) {
        Log.e(TAG, "translate: " + e.getMessage());
        service.updateUI(
            FloatingWidgetService.UPDATE_UI_SHOW_RESULT,
            service.getResources().getString(R.string.translateFailed)
        );
      }
      catch (LanguageUnvailableException e) {
        service.updateUI(
            FloatingWidgetService.UPDATE_UI_SHOW_ERROR,
            service.getResources().getString(R.string.languageUnavailable)
        );
      }
      catch (Exception e) {
        Log.e(TAG, "translate: " + e.getMessage());
        e.printStackTrace();
        service.updateUI(
            FloatingWidgetService.UPDATE_UI_SHOW_RESULT,
            service.getResources().getString(R.string.translateFailed)
        );
      }

      isCapturing = false;
    };

    imageReader.setOnImageAvailableListener(imageAvailableListener, handler);
  }

  private void stopCapture() {

    if ( null != mediaProjection ) {

      mediaProjection.stop();
      mediaProjection = null;
      virtualDisplay.release();
    }
  }

  public void destroy() {

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
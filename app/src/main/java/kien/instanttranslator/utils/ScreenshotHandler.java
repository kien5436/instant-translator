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
import android.view.WindowManager;

import kien.instanttranslator.ocr.TesseractOCR;
import kien.instanttranslator.services.FloatingWidgetService;
import kien.instanttranslator.translation.LanguageModelMananger;

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
  private TesseractOCR tesseractOCR;

  private Context context;
  private int touchX;
  private int touchY;

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

    tesseractOCR = new TesseractOCR(context);
  }

  ScreenshotHandler() {}

  public void setTouchX(int touchX) { this.touchX = touchX; }

  public void setTouchY(int touchY) { this.touchY = touchY; }

  WindowManager getWindowManager() { return FloatingWidgetService.getWindowManager(); }

  public void startCapture() {

    if ( null != mediaProjection || null == resultData )
      return; // hmm, I feel something is not good here

    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
    final Screenshot screenshot = Screenshot.getInstance();
    final ImageReader imageReader = ImageReader
        .newInstance(screenshot.getWidth(), screenshot.getHeight(), PixelFormat.RGBA_8888, 2);
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

    final ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {

      @Override
      public void onImageAvailable(ImageReader reader) {

        Bitmap bitmap = screenshot.getImage(imageReader);
        stopCapture();
        ((FloatingWidgetService) context)
            .updateUI(FloatingWidgetService.UPDATE_UI_SHOW_FOCUS_VIEW, null);

        // do OCR
        tesseractOCR.setLanguage(LanguageModelMananger.DEFAULT_LANGUAGE);
        String extracted = tesseractOCR
            .extractText(bitmap, touchX, touchY);
        Log.d(TAG, "onImageAvailable: " + extracted);
        ((FloatingWidgetService) context)
            .updateUI(FloatingWidgetService.UPDATE_UI_SHOW_RESULT, extracted);
      }
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
    tesseractOCR.destroy();
  }
}

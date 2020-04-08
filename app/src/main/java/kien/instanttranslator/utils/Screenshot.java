package kien.instanttranslator.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import kien.instanttranslator.services.FloatingWidgetService;

public class Screenshot implements ImageReader.OnImageAvailableListener {

  private final String TAG = getClass().getSimpleName();
  private final ImageReader imageReader;
  private final FloatingWidgetService service;
  private final int width, height;

  private static Screenshot screenshot = null;

  private Bitmap latestBmp = null;

  public static Screenshot getInstance(FloatingWidgetService service) {

    if ( null == screenshot )
      screenshot = new Screenshot(service);

    return screenshot;
  }

  private Screenshot(FloatingWidgetService service) {

    this.service = service;

    Display display = service.getWindowManager().getDefaultDisplay();
    Point size = new Point();

    display.getRealSize(size);

    int width = size.x;
    int height = size.y;

    while (width * height > (2 << 19)) {

      width = width >> 1;
      height = height >> 1;
    }

    this.width = width;
    this.height = height;
    imageReader = ImageReader.newInstance(this.width, this.height, PixelFormat.RGBA_8888, 2);

    imageReader.setOnImageAvailableListener(this, service.getHandler());
  }

  public int getWidth() { return width; }

  public int getHeight() { return height; }

  public Surface getSurface() { return imageReader.getSurface(); }

  @Override
  public void onImageAvailable(ImageReader reader) {

    final Image image = imageReader.acquireLatestImage();

    if ( null != image ) {

      Image.Plane[] planes = image.getPlanes();
      ByteBuffer buffer = planes[0].getBuffer();
      int pixelStride = planes[0].getPixelStride();
      int rowStride = planes[0].getRowStride();
      int rowPadding = rowStride - pixelStride * width;
      int bitmapWidth = width + rowPadding / pixelStride;

      if ( null == latestBmp || latestBmp.getWidth() != bitmapWidth || latestBmp
                                                                           .getHeight() != height ) {

        if ( null != latestBmp ) latestBmp.recycle();

        latestBmp = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888);
      }

      latestBmp.copyPixelsFromBuffer(buffer);
      image.close();

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Bitmap croppedBmp = Bitmap.createBitmap(latestBmp, 0, 0, width, height);

      croppedBmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

      byte[] newImage = outputStream.toByteArray();

      service.showCapturedImage(croppedBmp);
      saveImage(newImage);
    }
  }

  public void saveImage(final byte[] image) {

    new Thread() {

      @Override
      public void run() {

        File outputFile = new File(service.getExternalFilesDir(null) + "/screenshot.jpg");

        try {
          FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

          fileOutputStream.write(image);
          fileOutputStream.flush();
          fileOutputStream.getFD().sync();
          fileOutputStream.close();

          Log.d(TAG, "run: saved image at " + service.getExternalFilesDir(null));
//          showCapturedImage(outputFile);
        }
        catch (Exception e) {

          Log.e(TAG, "save: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }.start();

    service.stopCapture();
  }
  public Bitmap getImage()
  {
    File imgFile = new  File(service.getExternalFilesDir(null) + "/screenshot.jpg");
    Bitmap myBitmap=null;
    if(imgFile.exists()) {

      myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    }
      return myBitmap;
  }
}

package kien.instanttranslator.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.view.Display;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;


class Screenshot {

  private final String TAG = getClass().getSimpleName();
  private int width, height;

  static final float SCALE_RATIO = (float) 1;

  private static Screenshot screenshot = null;

  private Bitmap latestBmp = null;

  static Screenshot getInstance(Display display) {

    if ( null == screenshot )
      screenshot = new Screenshot(display);

    return screenshot;
  }

  private Screenshot(Display display) {

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
  }

  int getWidth() { return width; }

  int getHeight() { return height; }

  Bitmap getImage(ImageReader reader) {

    final Image image = reader.acquireLatestImage();

    if ( null == image ) return null;

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
    croppedBmp = grayedOut(croppedBmp);
    croppedBmp = Bitmap
        .createScaledBitmap(croppedBmp,
            (int) (croppedBmp.getWidth() * SCALE_RATIO),
            (int) (croppedBmp.getHeight() * SCALE_RATIO),
            true);

    croppedBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);

    byte[] newImage = outputStream.toByteArray();

    return BitmapFactory.decodeByteArray(newImage, 0, newImage.length);
  }

  private Bitmap grayedOut(Bitmap srcBmp) {

    Bitmap bwBmp = Bitmap
        .createBitmap(srcBmp.getWidth(), srcBmp.getHeight(), srcBmp.getConfig());
    Canvas canvas = new Canvas(bwBmp);
    Paint paint = new Paint();
    ColorMatrix colorMatrix = new ColorMatrix();

    colorMatrix.setSaturation(0);
    paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    canvas.drawBitmap(srcBmp, 0, 0, paint);

    return bwBmp;
  }
}

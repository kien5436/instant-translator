package kien.instanttranslator.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class OCR {

  private final String TAG = getClass().getSimpleName();

  private Context context;

  public OCR(Context context) {

    this.context = context;
  }

  public void takeScreenshot(int x, int y, int width, int height) {

    Date now = new Date();
    android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

    try {
      // image naming and path  to include sd card  appending name you choose for file
      String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

      // create bitmap screen capture
      View v1 = ((Activity) context).getWindow().getDecorView().getRootView();
      v1.setDrawingCacheEnabled(true);
      Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache(), x, y, width, height);
      v1.setDrawingCacheEnabled(false);

      File imageFile = new File(mPath);

      FileOutputStream outputStream = new FileOutputStream(imageFile);
      int quality = 100;
      bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
      outputStream.flush();
      outputStream.close();
      Log.d(TAG, "takeScreenshot: done" );
      openScreenshot(imageFile);
    }
    catch (Throwable e) {
      Log.e(TAG, "takeScreenshot: " + e.getMessage() );
    }
  }

  private void openScreenshot(File imageFile) {

    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_VIEW);
    Uri uri = Uri.fromFile(imageFile);
    intent.setDataAndType(uri, "image/*");
    context.startActivity(intent);
  }
}

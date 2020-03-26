package kien.instanttranslator.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
public class TesseractOCR {

  private final TessBaseAPI tessBaseAPI = new TessBaseAPI();
  private final String TAG = getClass().getSimpleName();

  // copy trained data from APK to internal storage
  public TesseractOCR(Context context, String language) {

    boolean fileExist = false;
    AssetManager assetManager = context.getAssets();
    String destPathDir = context.getFilesDir() + "/tesseract/tessdata/";
    String srcFile = "eng.traineddata";
    InputStream inputStream = null;
    FileOutputStream outFile = null;
    String destInitPathDir = context.getFilesDir() + "/tesseract";
    String destPathFile = destPathDir + srcFile;

    try {
      inputStream = assetManager.open(srcFile);
      File file = new File(destPathDir);

      if ( !file.exists() ) {

        if ( !file.mkdirs() )
          Log.d(TAG, "TesseractOCR: " + srcFile + " can't be created");

        outFile = new FileOutputStream(new File(destPathFile));
      }
      else fileExist = true;
    }
    catch (Exception e) {
      Log.e(TAG, "TesseractOCR: " + e.getMessage());
    }
    finally {
      if ( fileExist ) {

        try {
          if ( null != inputStream ) inputStream.close();
          tessBaseAPI.init(destInitPathDir, language);
          return;
        }
        catch (Exception e) {
          Log.e(TAG, "TesseractOCR: " + e.getMessage());
        }
      }

      if ( null != inputStream && null != outFile ) {

        try {
          byte[] buff = new byte[1024];
          int len;

          while (-1 != (len = inputStream.read(buff))) outFile.write(buff, 0, len);

          inputStream.close();
          outFile.close();
          tessBaseAPI.init(destInitPathDir, language);
        }
        catch (Exception e) {
          Log.e(TAG, "TesseractOCR: " + e.getMessage());
        }
      }
      else Log.d(TAG, "TesseractOCR: " + srcFile + " can't be read");
    }
  }

  public String getResult(Bitmap bitmap) {

    tessBaseAPI.setImage(bitmap);
    return tessBaseAPI.getUTF8Text();
  }

  public void onDestroy() { if ( null != tessBaseAPI ) tessBaseAPI.end(); }
}

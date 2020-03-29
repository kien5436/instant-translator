package kien.instanttranslator.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kien.instanttranslator.utils.PermissionManager;
public class TesseractOCR {

  private final String TAG = getClass().getSimpleName();
  private TessBaseAPI tessBaseAPI;
  private final String APP_STORAGE_PATH;

  private String language;

  public TesseractOCR(Context context) {

    this.APP_STORAGE_PATH = context.getFilesDir().getAbsolutePath() + "/instant_translator/";
    copyTessData(context);
  }

  public void setLanguage(String language) {

    this.language = language;
  }

  // copy trained data from APK to internal storage
  private void copyTessData(Context context) {

    final String TESSDATA_PATH = "tessdata";

    File tessDataDir = new File(APP_STORAGE_PATH + TESSDATA_PATH);

    // create TESSDATA_PATH if not exist
    if ( !tessDataDir.exists() && !tessDataDir.mkdirs() )
      PermissionManager.askPermissions(context);

    try {
      String[] tessDataList = context.getAssets().list(TESSDATA_PATH);

      if ( null != tessDataList )
        for (String fileName : tessDataList) {

          String outputFilePath = APP_STORAGE_PATH + TESSDATA_PATH + "/" + fileName;

          if ( !(new File(outputFilePath)).exists() ) {

            InputStream is = context.getAssets().open(TESSDATA_PATH + "/" + fileName);
            OutputStream os = new FileOutputStream(outputFilePath);
            byte[] buff = new byte[1024];
            int len;

            for (; ; ) {

              len = is.read(buff);

              if ( len <= 0 ) break;

              os.write(buff, 0, len);
            }

            is.close();
            os.flush();
            os.close();
          }
        }
    }
    catch (IOException e) {

      Log.e(TAG, "copyTessData: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public String extractText(Bitmap bitmap) {

    String res = "";

    try {

      tessBaseAPI = new TessBaseAPI();

      tessBaseAPI.init(this.APP_STORAGE_PATH, this.language);
      tessBaseAPI.setImage(bitmap);
      res = tessBaseAPI.getUTF8Text();
      tessBaseAPI.end();
    }
    catch (Exception e) {

      Log.e(TAG, "extractText: " + e.getMessage());
      e.printStackTrace();
    }

    return res;
  }

  public void onDestroy() { if ( null != tessBaseAPI ) tessBaseAPI.end(); }
}

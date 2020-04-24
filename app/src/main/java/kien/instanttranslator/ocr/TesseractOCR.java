package kien.instanttranslator.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kien.instanttranslator.BuildConfig;
import kien.instanttranslator.utils.PermissionManager;

import static com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_WORD;

public class TesseractOCR {

  private final String TAG = getClass().getSimpleName();
  private static TessBaseAPI tessBaseAPI = null;
  private static String APP_STORAGE_PATH;

  private String language;

  public TesseractOCR(Context context) {

    tessBaseAPI = new TessBaseAPI();
    APP_STORAGE_PATH = context.getFilesDir() + "/";

    copyTessData(context);
  }

  public void setLanguage(String language) { this.language = language;}

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

            for (int len = 1; ; len = is.read(buff)) {

              if ( 0 >= len ) break;

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

  public String extractText(Bitmap bitmap, int x, int y) {

    String result = null;

    try {
//      tessBaseAPI = new TessBaseAPI();
      tessBaseAPI.init(APP_STORAGE_PATH, language, TessBaseAPI.OEM_DEFAULT);
      tessBaseAPI.setDebug(BuildConfig.DEBUG);
      tessBaseAPI.setImage(bitmap);
      Log.d(TAG, "extractText: " + tessBaseAPI.getUTF8Text());
      ResultIterator resultIterator = tessBaseAPI.getResultIterator();

      if ( null != resultIterator ) {

        int size = tessBaseAPI.getWords().size();
        int i = 0;

        do {
          Rect rect = tessBaseAPI.getWords().getBox(i).getRect();

          if ( rect.contains(x, y) ) {

            result = resultIterator.getUTF8Text(RIL_WORD);
            resultIterator.delete();
            break;
          }

          i++;
        } while (i < size && resultIterator.next(RIL_WORD));
      }
    }
    catch (Exception e) {
      Log.e(TAG, "extractText: " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      tessBaseAPI.clear();
      bitmap.recycle();
    }

    return result;
  }

  public void destroy() { if ( null != tessBaseAPI ) tessBaseAPI.end(); }
}
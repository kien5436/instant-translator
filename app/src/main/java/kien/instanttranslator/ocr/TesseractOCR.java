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

import kien.instanttranslator.utils.PermissionManager;

import static com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_WORD;

public class TesseractOCR {

    private final String TAG = getClass().getSimpleName();
    private TessBaseAPI tessBaseAPI;
    private final String APP_STORAGE_PATH;
    final String TESSDATA_PATH = "tessdata";

    private String language;

    public TesseractOCR(Context context) {

        this.APP_STORAGE_PATH = context.getExternalFilesDir(null) + "";
        copyTessData(context);
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(this.APP_STORAGE_PATH,"eng");
    }

    public void setLanguage(String language) {

        this.language = language;
    }

    // copy trained data from APK to internal storage
    private void copyTessData(Context context) {

        File tessDataDir = new File(APP_STORAGE_PATH +"/"+ TESSDATA_PATH);

        // create TESSDATA_PATH if not exist
        if ( !tessDataDir.exists() && !tessDataDir.mkdirs() )
            PermissionManager.askPermissions(context);

        try {
            String[] tessDataList = context.getAssets().list(TESSDATA_PATH);

            if ( null != tessDataList )
                for (String fileName : tessDataList) {

                    String outputFilePath = APP_STORAGE_PATH +"/"+ TESSDATA_PATH + "/" + fileName;

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

    public String extractText(Bitmap bitmap,int x,int y) {
        try {

            int index=0;
            tessBaseAPI.setImage(bitmap);
            String detectBlock = tessBaseAPI.getUTF8Text();
            //String[] abc=result.split("\\s");
            int size=tessBaseAPI.getWords().size();
            ResultIterator resultIterator=tessBaseAPI.getResultIterator();
            do{
                //Box b=m_tess.getWords().getBox(j);
                Rect rect=tessBaseAPI.getWords().getBox(index).getRect();
                String detectWord=resultIterator.getUTF8Text(RIL_WORD);
                if(rect.contains(x,y))
                {
                    resultIterator.delete();
                    return detectWord;
                }
                index++;
            }
            while(resultIterator.next(RIL_WORD)&& index<size);
        }
        catch (Exception e) {
            Log.e(TAG, "extractText: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public void onDestroy() { if ( null != tessBaseAPI ) tessBaseAPI.end(); }
}

package kien.instanttranslator.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import kien.instanttranslator.R;
import kien.instanttranslator.ocr.TesseractOCR;

public class OCRActivity extends AppCompatActivity {

  Button btnScan;
  TextView tvOCR;
  ImageView imgOCR;

  private ProgressDialog progressDialog;
  private TesseractOCR tesseractOCR;
  private Context context;
  protected String currentPhotoPath;
  private Uri photoURI, oldPhotoURI;

  private static final String errorFileCreate = "Error file create!";
  private static final String errorConvert = "Error on converting photo to bitmap!";
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final String TAG = "OCRActivity";

  int PERMISSION_ALL = 1;
  boolean hasPermissions = false;

  String[] PERMISSIONS = {
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.CAMERA
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ocr);

    context = OCRActivity.this;
    bindView();

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    if ( !hasPermissions ) checkPermission();

    String language = "eng";
    tesseractOCR = new TesseractOCR(this);
  }

  private void bindView() {

    imgOCR = findViewById(R.id.imgOCR);
    tvOCR = findViewById(R.id.tvOCR);
    btnScan = findViewById(R.id.btnScan);

    btnScan.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {

        scanPhoto();
      }
    });
  }

  private void scanPhoto() {

    if ( !hasPermissions ) {

      checkPermission();
      return;
    }

    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    if ( null != takePictureIntent.resolveActivity(context.getPackageManager()) ) {

      File photoFile = null;

      try {
        photoFile = createImageFile();
      }
      catch (IOException e) {
        Log.e(TAG, "scanPhoto: " + e.getMessage());
        Toast.makeText(context, errorFileCreate, Toast.LENGTH_SHORT).show();
      }

      if ( null != photoFile ) {

        oldPhotoURI = photoURI;
        photoURI = Uri.fromFile(photoFile);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
      }
    }
  }

  private File createImageFile() throws IOException {

    String timestamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());
    String imageFileName = "OCR_" + timestamp + "_";
    File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File image = File.createTempFile(imageFileName, ".jpg", storageDir);

    currentPhotoPath = image.getAbsolutePath();

    return image;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    if ( REQUEST_IMAGE_CAPTURE == requestCode ) {

      if ( RESULT_OK == resultCode ) {

        Bitmap bmp = null;

        try {
          InputStream inputStream = context.getContentResolver().openInputStream(photoURI);
          BitmapFactory.Options options = new BitmapFactory.Options();
          bmp = BitmapFactory.decodeStream(inputStream, null, options);
        }
        catch (Exception e) {
          Log.e(TAG, "onActivityResult: " + e.getMessage());
          Toast.makeText(context, errorConvert, Toast.LENGTH_SHORT).show();
        }

        imgOCR.setImageBitmap(bmp);
        doOCR(bmp);

        try {
          OutputStream outputStream = new FileOutputStream(photoURI.getPath());

          if ( null != bmp ) bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
          outputStream.flush();
          outputStream.close();
        }
        catch (Exception e) {
          Log.e(TAG, "onActivityResult: " + e.getMessage());
          Toast.makeText(context, errorFileCreate, Toast.LENGTH_SHORT).show();
        }
      }
      else {
        photoURI = oldPhotoURI;
        imgOCR.setImageURI(photoURI);
      }
    }
  }

  private void doOCR(final Bitmap bmp) {

    if ( null == progressDialog )
      progressDialog = progressDialog.show(context, "Processing", "Doing OCR...", true);
    else progressDialog.show();

    // create a thread cuz OCR process can take a long time
    new Thread(new Runnable() {

      @Override
      public void run() {

        final String ocrResult = tesseractOCR.extractText(bmp);
        Log.d(TAG, "run: " + ocrResult);

        runOnUiThread(new Runnable() {

          @Override
          public void run() {

            if ( null != ocrResult && !ocrResult.equals("") )
              tvOCR.setText(ocrResult);

            progressDialog.dismiss();
          }
        });
      }
    });
  }

  private void checkPermission() {

    if ( !granted(context, PERMISSIONS) ) {

      requestPermissions(PERMISSIONS, PERMISSION_ALL);
      hasPermissions = false;
    }

    hasPermissions = true;
  }

  private boolean granted(Context context, String... permissions) {

    if ( null != context && null != permissions )
      for (String perm : permissions) {
        if ( PackageManager.PERMISSION_GRANTED != ActivityCompat
            .checkSelfPermission(context, perm) )
          return false;
      }

    return true;
  }
}

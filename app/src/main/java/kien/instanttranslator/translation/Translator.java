package kien.instanttranslator.translation;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Translator {

  private final String TAG = getClass().getSimpleName();

  private String originalText;
  private int targetLanguage;

  public Translator() {

    targetLanguage = FirebaseTranslateLanguage.VI;
  }

  public void setTargetLanguage(int targetLanguage) { this.targetLanguage = targetLanguage; }

  public void setOriginalText(String originalText) { this.originalText = originalText; }

  public String translate() {

    String translatedText = null;
    FirebaseLanguageIdentification languageIdentifier = FirebaseNaturalLanguage.getInstance()
                                                                               .getLanguageIdentification();
    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build();
    final Task<String> identifyTask = languageIdentifier.identifyLanguage(originalText);

    try {
      String languageCode = Tasks.await(identifyTask, 500, TimeUnit.MILLISECONDS);
      Integer sourceLanguage = FirebaseTranslateLanguage.languageForLanguageCode(languageCode);

      // set fallback source language to English
      if ( null == sourceLanguage ) sourceLanguage = FirebaseTranslateLanguage.EN;

      FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
          .setSourceLanguage(sourceLanguage)
          .setTargetLanguage(targetLanguage)
          .build();
      final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                                                                   .getTranslator(options);
      final Task<Void> downloadTask = translator.downloadModelIfNeeded(conditions);
      final Task<String> translateTask = translator.translate(originalText);

      Tasks.await(downloadTask);
      translatedText = Tasks.await(translateTask);
    }
    catch (InterruptedException | ExecutionException e) {
      Log.e(TAG, "translate: " + e.getMessage());
      e.printStackTrace();
    }
    catch (TimeoutException e) {
      e.printStackTrace();
    }

    return translatedText;
  }
}

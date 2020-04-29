package kien.instanttranslator.translation;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
<<<<<<< HEAD
=======
<<<<<<< HEAD
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
=======
>>>>>>> 16fbd083fe99d76de434b6a003a0e96c83b3153d
>>>>>>> master
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

  public String translate() throws InterruptedException, ExecutionException, TimeoutException {

    FirebaseLanguageIdentification languageIdentifier = FirebaseNaturalLanguage.getInstance()
                                                                               .getLanguageIdentification();
    final Task<String> identifyTask = languageIdentifier.identifyLanguage(originalText);

    String languageCode = Tasks.await(identifyTask, 500, TimeUnit.MILLISECONDS);
    Integer sourceLanguage = !languageCode
        .equals(FirebaseLanguageIdentification.UNDETERMINED_LANGUAGE_CODE) ?
                             FirebaseTranslateLanguage.languageForLanguageCode(languageCode) :
                             null;

    // set fallback source language to English
    if ( null == sourceLanguage ) sourceLanguage = FirebaseTranslateLanguage.EN;

    FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
        .setSourceLanguage(sourceLanguage)
        .setTargetLanguage(targetLanguage)
        .build();
<<<<<<< HEAD
=======
<<<<<<< HEAD
    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build();
    final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                                                                 .getTranslator(options);
    final Task<Void> downloadTask = translator.downloadModelIfNeeded(conditions);
    final Task<String> translateTask = translator.translate(originalText);

    Tasks.await(downloadTask);
=======
>>>>>>> master
//    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
//        .requireWifi()
//        .build();
    final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                                                                 .getTranslator(options);
//    final Task<Void> downloadTask = translator.downloadModelIfNeeded(conditions);
    final Task<String> translateTask = translator.translate(originalText);

//    Tasks.await(downloadTask);
<<<<<<< HEAD
=======
>>>>>>> 16fbd083fe99d76de434b6a003a0e96c83b3153d
>>>>>>> master

    return Tasks.await(translateTask);
  }
}
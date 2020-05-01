package kien.instanttranslator.translation;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import kien.instanttranslator.utils.LanguageUnvailableException;
import kien.instanttranslator.utils.Network;

public class Translator {

  private final String TAG = getClass().getSimpleName();

  private String originalText;
  private int targetLanguage;

  private Context context;
  private LanguageModelMananger languageModelMananger;
  private FirebaseLanguageIdentification languageIdentifier;

  public Translator(Context context) {

    this.context = context;
    targetLanguage = FirebaseTranslateLanguage.VI;
    languageModelMananger = new LanguageModelMananger();
    languageIdentifier = FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
  }

  public void setTargetLanguage(int targetLanguage) { this.targetLanguage = targetLanguage; }

  public void setOriginalText(String originalText) { this.originalText = originalText; }

  public String translate()
      throws InterruptedException, ExecutionException, TimeoutException, LanguageUnvailableException {

    final Task<String> identifyTask = languageIdentifier.identifyLanguage(originalText);
    String languageCode = Tasks.await(identifyTask, 500, TimeUnit.MILLISECONDS);
    Integer sourceLanguage = !languageCode
        .equals(FirebaseLanguageIdentification.UNDETERMINED_LANGUAGE_CODE) ?
                             FirebaseTranslateLanguage.languageForLanguageCode(languageCode) :
                             null;

    // set fallback source language to English
    if ( null == sourceLanguage ) sourceLanguage = FirebaseTranslateLanguage.EN;

    if ( (!languageModelMananger.isDownloaded(sourceLanguage) ||
          !languageModelMananger.isDownloaded(targetLanguage))
         && !Network.isAvailable(context) )
      throw new LanguageUnvailableException();

    FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
        .setSourceLanguage(sourceLanguage)
        .setTargetLanguage(targetLanguage)
        .build();
    final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance()
                                                                 .getTranslator(options);
    final Task<String> translateTask = translator.translate(originalText);

    return Tasks.await(translateTask);
  }
}
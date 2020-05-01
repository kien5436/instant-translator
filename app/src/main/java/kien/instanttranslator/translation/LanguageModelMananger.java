package kien.instanttranslator.translation;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class LanguageModelMananger {

  private final String TAG = getClass().getSimpleName();

  private FirebaseModelManager modelManager;

  public LanguageModelMananger() {

    modelManager = FirebaseModelManager.getInstance();
  }

  public HashMap<Integer,String> getSupportedLanguages() {

    HashMap<Integer,String> supportedLanguages = new HashMap<>();
    final String[] SUPPORTED_LANGUAGES = { "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Danish", "German", "Greek", "English", "Esperanto", "Spanish", "Estonian", "Persian", "Finnish", "French", "Irish", "Galician", "Gujarati", "Hebrew", "Hindi", "Croatian", "Haitian", "Hungarian", "Indonesian", "Icelandic", "Italian", "Japanese", "Georgian", "Kannada", "Korean", "Lithuanian", "Latvian", "Macedonian", "Marathi", "Malay", "Maltese", "Dutch", "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Albanian", "Swedish", "Swahili", "Tamil", "Telugu", "Thai", "Tagalog", "Turkish", "Ukrainian", "Urdu", "Vietnamese", "Chinese" };
    Set<Integer> langCodes = FirebaseTranslateLanguage.getAllLanguages();
    int i = 0;

    for (Integer langCode: langCodes)
      supportedLanguages.put(langCode, SUPPORTED_LANGUAGES[i++]);

    return supportedLanguages;
  }

  public Set<FirebaseTranslateRemoteModel> getDownloadedLanguages()
      throws ExecutionException, InterruptedException {

    Task<Set<FirebaseTranslateRemoteModel>> getDownloadedModels = modelManager
        .getDownloadedModels(FirebaseTranslateRemoteModel.class);

    return Tasks.await(getDownloadedModels);
  }

  public boolean downloadLanguage(int langCode) throws ExecutionException, InterruptedException {

    FirebaseTranslateRemoteModel model = new FirebaseTranslateRemoteModel.Builder(langCode).build();
    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build();
    Task<Void> downloadModel = modelManager.download(model, conditions);

    Tasks.await(downloadModel);

    return downloadModel.isSuccessful();
  }

  public boolean deleteLanguage(int langCode) throws ExecutionException, InterruptedException {

    FirebaseTranslateRemoteModel model = new FirebaseTranslateRemoteModel.Builder(langCode).build();
    Task<Void> deleteModel = modelManager.deleteDownloadedModel(model);

    Tasks.await(deleteModel);

    return deleteModel.isSuccessful();
  }

  public boolean isDownloaded(int langCode) throws ExecutionException, InterruptedException {

    FirebaseTranslateRemoteModel model = new FirebaseTranslateRemoteModel.Builder(langCode).build();
    Task<Boolean> isModelDownloaded = modelManager.isModelDownloaded(model);

    return Tasks.await(isModelDownloaded);
  }
}

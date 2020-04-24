package kien.instanttranslator.translation;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;

import java.util.Set;

public class LanguageModelMananger {

  private final String TAG = getClass().getSimpleName();

  private FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
  public static final String DEFAULT_LANGUAGE = "eng";

  public void getDownloadedLanguages() {

    modelManager.getDownloadedModels(FirebaseTranslateRemoteModel.class)
                .addOnSuccessListener(new OnSuccessListener<Set<FirebaseTranslateRemoteModel>>() {

                  @Override
                  public void onSuccess(Set<FirebaseTranslateRemoteModel> models) {


                  }
                });
  }

  public Task<Void> downloadLanguage(int langCode) {

    FirebaseTranslateRemoteModel model = new FirebaseTranslateRemoteModel.Builder(langCode).build();
    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build();

    return modelManager.download(model, conditions);
  }

  public Task<Void> deleteLanguage(int langCode) {

    FirebaseTranslateRemoteModel model = new FirebaseTranslateRemoteModel.Builder(langCode).build();

    return modelManager.deleteDownloadedModel(model);
  }

//  public void getDownloadedLanguages() {
//
//    modelManager.getDownloadedModels(FirebaseTranslateRemoteModel.class)
//                .addOnSuccessListener(new OnSuccessListener<Set<FirebaseTranslateRemoteModel>>() {
//
//                  @Override
//                  public void onSuccess(Set<FirebaseTranslateRemoteModel> models) {
//
//                    Log.d(TAG, "onSuccess: downloaded languages:");
//                    for (FirebaseTranslateRemoteModel model : models) {
//                      Log.d(TAG, "onSuccess: " + model.getLanguageCode());
//                    }
//                  }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//
//                  @Override
//                  public void onFailure(@NonNull Exception e) {
//
//                    Log.e(TAG, "onFailure: " + e.getMessage());
//                    e.printStackTrace();
//                  }
//                });
//  }

//  public void downloadLanguage() {
//
//    FirebaseTranslateRemoteModel model = new FirebaseTranslateRemoteModel.Builder(FirebaseTranslateLanguage.VI)
//        .build();
//    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
//        .requireWifi()
//        .build();
//
//    modelManager.download(model, conditions)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//
//                  @Override
//                  public void onSuccess(Void aVoid) {
//
//                    Log.d(TAG, "onSuccess: successfully downloaded");
//                  }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//
//                  @Override
//                  public void onFailure(@NonNull Exception e) {
//
//                    Log.e(TAG, "onFailure: " + e.getMessage() );
//                    e.printStackTrace();
//                  }
//                });
//  }
}

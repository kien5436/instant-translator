package kien.instanttranslator.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import kien.instanttranslator.R;
import kien.instanttranslator.translation.LanguageModel;
import kien.instanttranslator.translation.LanguageModelMananger;

public class LanguagesFragment extends Fragment {

  private final String TAG = getClass().getSimpleName();

  private Context context;
  private HandlerThread handlerThread;
  private Handler handler;

  private LanguageModelMananger languageModelMananger;
  private HashMap<Integer, String> supportedLanguages;
  private List<LanguageModel> languageModels;

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_languages, container, false);
    ListView lvLanguages = view.findViewById(R.id.lvLanguages);
    context = this.getContext();
    handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
    languageModelMananger = new LanguageModelMananger();
    languageModels = new ArrayList<>();
    supportedLanguages = languageModelMananger.getSupportedLanguages();

    bindLanguageList(lvLanguages, view);

    lvLanguages.setOnItemLongClickListener((parent, _view, position, id) -> {

      LanguageModel languageModel = (LanguageModel) parent.getItemAtPosition(position);
      AppCompatImageButton icon = lvLanguages
          .getChildAt(position - lvLanguages.getFirstVisiblePosition()).findViewById(R.id.btnIcon);

      deleteLanguage(languageModel, icon);

      return false;
    });

    view.findViewById(R.id.btnBack).setOnClickListener(v -> destroyFragment());

    return view;
  }

  private void deleteLanguage(LanguageModel languageModel, AppCompatImageButton iconView) {

    if ( !languageModel.isDownloaded() ) return;

    new AlertDialog.Builder(context)
        .setTitle("")
        .setMessage(context.getResources().getString(R.string.deleteLanguage))
        .setNegativeButton(R.string.no, null)
        .setPositiveButton(R.string.yes,
            (dialog, which) -> {

              handler.post(() -> {

                boolean success = false;
                try {
                  success = languageModelMananger.deleteLanguage(languageModel.getLanguageCode());
                }
                catch (ExecutionException | InterruptedException e) {
                  Log.e(TAG, "deleteLanguage: " + e.getMessage());
                }

                if ( success )
                  iconView.post(() -> iconView.setImageResource(R.drawable.ic_arrow_downward_24dp));
                else
                  Toast.makeText(context, context.getResources()
                                                 .getString(R.string.deleteLanguageFailure), Toast.LENGTH_SHORT)
                       .show();
              });
            })
        .show();
  }

  private void bindLanguageList(final ListView listView, final View parentView) {

    handler.post(() -> {

      for (Map.Entry<Integer, String> lang : supportedLanguages.entrySet()) {

        try {
          boolean isDownloaded = languageModelMananger.isDownloaded(lang.getKey());

          languageModels.add(new LanguageModel(lang.getValue(), lang.getKey(), isDownloaded));
        }
        catch (ExecutionException | InterruptedException e) {
          Log.e(TAG, "bindLanguageList: " + e.getMessage());
        }
      }

      Collections
          .sort(languageModels, (o1, o2) -> o1.getLanguage().compareToIgnoreCase(o2.getLanguage()));

      LanguageAdapter adapter = new LanguageAdapter.Builder()
          .setContext(context)
          .setHandler(handler)
          .setLanguageModelList(languageModels)
          .setLanguageModelManager(languageModelMananger)
          .setParentView(parentView)
          .build();

      parentView.post(() -> { listView.setAdapter(adapter); });
    });
  }

  private void destroyFragment() {

    if ( null == getActivity() ) return;

    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

    ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down)
      .remove(this)
      .commit();

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    }
    catch (InterruptedException e) {
      Log.e(TAG, "destroyFragment: " + e.getMessage());
    }
  }
}

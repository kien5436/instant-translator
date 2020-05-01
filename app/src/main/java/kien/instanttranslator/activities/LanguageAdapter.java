package kien.instanttranslator.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import kien.instanttranslator.R;
import kien.instanttranslator.translation.LanguageModel;
import kien.instanttranslator.translation.LanguageModelMananger;
import kien.instanttranslator.utils.Network;

public class LanguageAdapter extends BaseAdapter {

  private final String TAG = LanguageAdapter.class.getSimpleName();

  private LayoutInflater inflater;
  private List<LanguageModel> languageModelList;
  private Context context;
  private Handler handler;
  private View parentView;
  private LanguageModelMananger languageModelMananger;

  private LanguageAdapter(Builder builder) {

    context = builder.context;
    handler = builder.handler;
    languageModelList = builder.languageModelList;
    parentView = builder.parentView;
    languageModelMananger = builder.languageModelMananger;
    inflater = LayoutInflater.from(context);
  }

  @Override
  public int getCount() { return languageModelList.size(); }

  @Override
  public LanguageModel getItem(int i) { return languageModelList.get(i); }

  @Override
  public long getItemId(int position) { return position; }

  @SuppressLint("InflateParams")
  @Override
  public View getView(int i, View convertView, ViewGroup parent) {

    ViewHolder viewHolder;
    LanguageModel languageModel = languageModelList.get(i);
    View progressDialog = parentView.findViewById(R.id.progressDialog);

    if ( null == convertView ) {

      viewHolder = new ViewHolder();
      convertView = inflater.inflate(R.layout.language_item, null);
      viewHolder.tvLanguage = convertView.findViewById(R.id.tvLanguage);
      viewHolder.btnIcon = convertView.findViewById(R.id.btnIcon);

      convertView.setTag(viewHolder);
    }
    else viewHolder = (ViewHolder) convertView.getTag();

    viewHolder.tvLanguage.setText(languageModel.getLanguage());
    viewHolder.btnIcon
        .setImageResource(languageModel.isDownloaded() ?
                          R.drawable.ic_check_24dp : R.drawable.ic_arrow_downward_24dp);
    viewHolder.btnIcon.setFocusable(false);
    viewHolder.btnIcon.setFocusableInTouchMode(false);
    viewHolder.btnIcon
        .setOnClickListener(v -> downloadLanguage(languageModel, v, progressDialog));

    return convertView;
  }


  private void downloadLanguage(LanguageModel languageModel, View view, View progressDialog) {

    if ( languageModel.isDownloaded() ) return;

    if ( !Network.isAvailable(context) ) {

      Toast.makeText(context,
          context.getResources().getString(R.string.requireNetwork), Toast.LENGTH_SHORT).show();
      return;
    }

    progressDialog.setVisibility(View.VISIBLE);

    handler.post(() -> {

      String msg;
      boolean success = false;
      try {
        success = languageModelMananger.downloadLanguage(languageModel.getLanguageCode());
      }
      catch (ExecutionException | InterruptedException e) {
        Log.e(TAG, "downloadLanguage: " + e.getMessage());
      }
      boolean finalSuccess = success;

      msg = success ? context.getResources().getString(R.string.downloadLanguageSuccess) :
            context.getResources().getString(R.string.downloadLanguageFailure);

      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

      view.post(() -> {

        progressDialog.setVisibility(View.GONE);
        if ( finalSuccess )
          ((AppCompatImageButton) view).setImageResource(R.drawable.ic_check_24dp);
      });
    });
  }

  static class ViewHolder {

    MaterialTextView tvLanguage;
    AppCompatImageButton btnIcon;
  }

  static class Builder {

    private Context context;
    private Handler handler;
    private List<LanguageModel> languageModelList;
    private View parentView;
    private LanguageModelMananger languageModelMananger;

    Builder setLanguageModelManager(LanguageModelMananger languageModelManager) {

      this.languageModelMananger = languageModelManager;
      return this;
    }

    Builder setParentView(View parentView) {

      this.parentView = parentView;
      return this;
    }

    Builder setLanguageModelList(List<LanguageModel> languageModelList) {

      this.languageModelList = languageModelList;
      return this;
    }

    Builder setContext(Context context) {

      this.context = context;
      return this;
    }

    Builder setHandler(Handler handler) {

      this.handler = handler;
      return this;
    }

    LanguageAdapter build() {

      return new LanguageAdapter(this);
    }
  }
}

package kien.instanttranslator.ocr;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import kien.instanttranslator.utils.LowStorageException;

public class MobileVisionAPI {

  private final String TAG = getClass().getSimpleName();

  private Context context;
  private TextRecognizer textRecognizer;

  public MobileVisionAPI(Context context) {

    this.context = context;
    textRecognizer = new TextRecognizer.Builder(context).build();
  }

  public String extract(Context context, Bitmap bitmap, float x, float y) {

    TextRecognizer recognizer = new TextRecognizer.Builder(context).build();
    StringBuilder sb = new StringBuilder();

    if ( recognizer.isOperational() ) {
      Frame frame = new Frame.Builder().setBitmap(bitmap).build();

      SparseArray items = recognizer.detect(frame);
      List<TextBlock> blocks = new List<TextBlock>() {

        @Override
        public int size() {

          return 0;
        }

        @Override
        public boolean isEmpty() {

          return false;
        }

        @Override
        public boolean contains(@Nullable Object o) {

          return false;
        }

        @NonNull
        @Override
        public Iterator<TextBlock> iterator() {

          return null;
        }

        @NonNull
        @Override
        public Object[] toArray() {

          return new Object[0];
        }

        @NonNull
        @Override
        public <T> T[] toArray(@NonNull T[] a) {

          return null;
        }

        @Override
        public boolean add(TextBlock textBlock) {

          return false;
        }

        @Override
        public boolean remove(@Nullable Object o) {

          return false;
        }

        @Override
        public boolean containsAll(@NonNull Collection<?> c) {

          return false;
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends TextBlock> c) {

          return false;
        }

        @Override
        public boolean addAll(int index, @NonNull Collection<? extends TextBlock> c) {

          return false;
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {

          return false;
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {

          return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public TextBlock get(int index) {

          return null;
        }

        @Override
        public TextBlock set(int index, TextBlock element) {

          return null;
        }

        @Override
        public void add(int index, TextBlock element) {

        }

        @Override
        public TextBlock remove(int index) {

          return null;
        }

        @Override
        public int indexOf(@Nullable Object o) {

          return 0;
        }

        @Override
        public int lastIndexOf(@Nullable Object o) {

          return 0;
        }

        @NonNull
        @Override
        public ListIterator<TextBlock> listIterator() {

          return null;
        }

        @NonNull
        @Override
        public ListIterator<TextBlock> listIterator(int index) {

          return null;
        }

        @NonNull
        @Override
        public List<TextBlock> subList(int fromIndex, int toIndex) {

          return null;
        }
      };

      TextBlock myItem = null;
      for (int i = 0; i < items.size(); ++i) {
        myItem = (TextBlock) items.valueAt(i);
        blocks.add(myItem);
        sb.append(myItem.getValue());
        sb.append("\n");
        List<Text> lineList = (List<Text>) myItem.getComponents();
        //Loop through each `Line`
        for (int j = 0; j < lineList.size(); ++j) {
          Text line = lineList.get(j);
          List<Text> wordList = (List<Text>) line.getComponents();
          //Loop through each `Word`
          for (int k = 0; k < wordList.size(); ++k) {
            Text word = wordList.get(k);
            //Get the Rectangle/boundingBox of the word
            RectF rectF = new RectF(word.getBoundingBox());
            if ( rectF.contains(x, y) ) {
              return word.getValue();
            }
          }
        }
      }

      Log.e("abc", sb.toString());
    }

    return null;
  }

  public String extractText(Bitmap bitmap, float x, float y)
      throws LowStorageException {

    String word = null;
    boolean hasAbility = false;

    if ( !textRecognizer.isOperational() ) {

      // Check for low storage.  If there is low storage, the native library will not be
      // downloaded, so detection will not become operational.
      IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
      boolean hasLowStorage = null != context.registerReceiver(null, lowstorageFilter);

      if ( hasLowStorage ) throw new LowStorageException();
    }
    else {
      Frame frame = new Frame.Builder().setBitmap(bitmap).build();
      SparseArray<TextBlock> blocks = textRecognizer.detect(frame);
      RectF boundingBox = new RectF();

      TextBlock block = null;
      for (int i = 0; i < blocks.size(); i++) {

        block = blocks.get(i);
        boundingBox.set(block.getBoundingBox());
//        Log.d(TAG, "block: " + block.getValue());
//        Log.d(TAG, "block: " + block.getBoundingBox());

        if ( boundingBox.contains(x, y) ) {

          hasAbility = true;
          break;
        }
      }

      if ( !hasAbility ) return null;

      List<Line> lines = (List<Line>) block.getComponents();
      Line line = null;
      hasAbility = false;

      for (int j = 0; j < lines.size(); j++) {

        line = lines.get(j);
        boundingBox.set(line.getBoundingBox());
//        Log.d(TAG, "line: " + line.getValue());
//        Log.d(TAG, "line: " + line.getBoundingBox());

        if ( boundingBox.contains(x, y) ) {

          hasAbility = true;
          break;
        }
      }

      if ( !hasAbility ) return null;

      List<Element> words = (List<Element>) line.getComponents();
      Element element;

      for (int k = 0; k < words.size(); k++) {

        element = words.get(k);
        boundingBox.set(element.getBoundingBox());
//        Log.d(TAG, "element: " + element.getValue());
//        Log.d(TAG, "element: " + element.getBoundingBox());

        if ( boundingBox.contains(x, y) ) {

          word = element.getValue();
          break;
        }
      }

      words.clear();
      lines.clear();
      blocks.clear();
    }

    return word;
  }

  public void destroy() { textRecognizer.release(); }
}

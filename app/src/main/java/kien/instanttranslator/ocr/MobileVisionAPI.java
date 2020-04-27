package kien.instanttranslator.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MobileVisionAPI {

    public String extract(Context context, Bitmap bitmap, float x, float y)
    {
        TextRecognizer recognizer = new TextRecognizer.Builder(context).build();
        StringBuilder sb = new StringBuilder();

        if (recognizer.isOperational())  {
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
            for (int i = 0; i < items.size(); ++i)
            {
                myItem = (TextBlock)items.valueAt(i);
                blocks.add(myItem);
                sb.append(myItem.getValue());
                sb.append("\n");
                List<Text> lineList = (List<Text>) myItem.getComponents();
                //Loop through each `Line`
                for(int j=0;j<lineList.size();++j)
                {
                    Text line=lineList.get(j);
                    List<Text> wordList = (List<Text>) line.getComponents();
                    //Loop through each `Word`
                    for(int k=0;k<wordList.size();++k)
                    {
                        Text word = wordList.get(k);
                        //Get the Rectangle/boundingBox of the word
                        RectF rectF = new RectF(word.getBoundingBox());
                        if ( rectF.contains(x, y) ) {
                            return word.getValue();
                        }
                    }
                }
            }

            Log.e("abc",sb.toString());
        }

        return null;
    }
}

package com.mediatek.bluetooth.sanitytest.history;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.bluetooth.sanitytest.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.Gallery.LayoutParams;

public class RecordGallery extends Activity implements
        AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory {
    private static final String TAG = "RecordGallery";

    private List<String> mImageList = new ArrayList<String>();
    private ImageSwitcher mSwitcher;

    private static final String mScanPath = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "SanityRecord"
            + File.separator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.record_gallery);

        mImageList = listScreenShot(mScanPath);

        if (mImageList == null || mImageList.isEmpty()) {
            Toast.makeText(this, "No available record, exit", Toast.LENGTH_LONG)
                    .show();
            this.finish();
            return;
        }

        mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(this);
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));

        Gallery gallery = (Gallery) findViewById(R.id.mygallery);
        gallery.setAdapter(new ImageAdapter(this, mImageList));
        gallery.setOnItemSelectedListener(this);
    }

    private List<String> listScreenShot(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files == null)
            return null;
        for (File file : files) {
            if (file.isDirectory()) {
                listScreenShot(file.getPath());
            } else {
                String fileName = file.getPath();
                String endPrefix = fileName.substring(
                        fileName.lastIndexOf(".") + 1, fileName.length())
                        .toLowerCase();
                if (endPrefix.equalsIgnoreCase("png")) {
                    mImageList.add(file.getPath());
                }
            }
        }
        return mImageList;
    }

    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;
        private List<String> lis;

        public ImageAdapter(Context context, List<String> list) {
            mContext = context;
            lis = list;
            TypedArray a = obtainStyledAttributes(R.styleable.Gallery);
            mGalleryItemBackground = a.getResourceId(
                    R.styleable.Gallery_android_galleryItemBackground, 0);
            a.recycle();
        }

        public int getCount() {
            return lis.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            Bitmap bm = BitmapFactory.decodeFile(lis.get(position).toString());
            i.setImageBitmap(bm);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new Gallery.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            i.setBackgroundResource(mGalleryItemBackground);
            return i;
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        String[] arrayList = mImageList.toArray(new String[mImageList.size()]);
        String photoURL = arrayList[position];
        mSwitcher.setImageURI(Uri.parse(photoURL));
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public View makeView() {
        ImageView i = new ImageView(this);
        i.setBackgroundColor(0xFF000000);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        return i;
    }
}

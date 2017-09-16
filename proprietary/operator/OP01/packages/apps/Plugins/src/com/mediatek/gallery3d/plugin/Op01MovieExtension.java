package com.mediatek.gallery3d.plugin;

import android.content.Context;

import com.mediatek.common.PluginImpl;
import com.mediatek.gallery3d.ext.DefaultMovieExtension;
import com.mediatek.gallery3d.ext.IActivityHooker;
import com.mediatek.gallery3d.ext.IServerTimeoutExtension;

import com.android.gallery3d.app.MovieActivity;

import java.util.ArrayList;

@PluginImpl(interfaceName="com.mediatek.gallery3d.ext.IMovieExtension")
public class Op01MovieExtension extends DefaultMovieExtension {
    private static final String TAG = "Op01MovieExtension";
    private static final boolean LOG = true;

    public Op01MovieExtension(Context context) {
        super(context);
    }

    @Override
    public boolean shouldEnableCheckLongSleep() {
        return false;
    }

    @Override
    public ArrayList<IActivityHooker> getHookers(Context context) {
        ArrayList<IActivityHooker> list = new ArrayList<IActivityHooker>();
        if (context instanceof MovieActivity) {
            list.add(new MovieListHooker(mContext));
        }
        list.add(new BookmarkHooker(mContext));
        list.add(new StereoAudioHooker(mContext));
        list.add(new StreamingHooker(mContext));
        return list;
    }

    @Override
    public IServerTimeoutExtension getServerTimeoutExtension() {
        return new ServerTimeout(mContext);
    }
}

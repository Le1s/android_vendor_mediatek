package com.mediatek.compatibility.facebeautyeffect;

import android.util.Log;

import com.mediatek.effect.EffectFactory;

public class FaceBeautyEffectSupport {

    public static final String TAG = "EffectSupport";
    public static boolean isFaceBeautyEffectFeatureAvaliable() {
        try {
            EffectFactory effectFactory = EffectFactory.createEffectFactory();
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "FaceBeautyEffect feature is not available");
            return false;
        }
    }
}

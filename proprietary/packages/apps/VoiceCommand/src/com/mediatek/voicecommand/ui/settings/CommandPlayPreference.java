/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.voicecommand.ui.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediatek.voicecommand.R;
import com.mediatek.xlog.Xlog;

/**
 * This class provides the View to be displayed in the VoiceUiCommandPlay and
 * associates with a SharedPreferences to store/retrieve the preference data.
 *
 */
public class CommandPlayPreference extends Preference {
    private static final String TAG = "CommandPlayPreference";

    private TextView mPreferenceTitle = null;

    private CharSequence mTitleValue = "";
    private LayoutInflater mInflater;
    private Context mContext;

    /**
     * Constructor of CommandPlayPreference.
     *
     * @param context
     *            the Context this is associated with
     * @param attrs
     *            the attributes of the XML tag that is inflating the preference
     * @param defStyle
     *            the default style to apply to this preference
     * @param title
     *            preference title
     */
    public CommandPlayPreference(Context context, AttributeSet attrs, int defStyle, String title) {
        super(context, attrs, defStyle);
        mContext = context;

        if (super.getTitle() != null) {
            mTitleValue = super.getTitle().toString();
        }
    }

    /**
     * Constructor of CommandPlayPreference.
     *
     * @param context
     *            the Context this is associated with
     * @param attrs
     *            the attributes of the XML tag that is inflating the preference
     */
    public CommandPlayPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    /**
     * Constructor of CommandPlayPreference.
     *
     * @param context
     *            the Context this is associated with
     */
    public CommandPlayPreference(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * Constructor of CommandPlayPreference.
     *
     * @param context
     *            the Context this is associated with
     * @param title
     *            preference title
     */
    public CommandPlayPreference(Context context, String title) {
        super(context);
        mContext = context;

        if (title != null) {
            mTitleValue = title;
        }
    }

    /**
     * Constructor of CommandPlayPreference.
     *
     * @param title
     *            preference title
     */
    public void setShowTitle(String title) {
        mTitleValue = title;
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        Xlog.d(TAG, "onCreateView");

        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.voice_ui_preference_title, null);

        mPreferenceTitle = (TextView) view.findViewById(R.id.command_preference_title);
        if (mPreferenceTitle != null) {
            mPreferenceTitle.setText(mTitleValue);
        }
        return view;
    }
}

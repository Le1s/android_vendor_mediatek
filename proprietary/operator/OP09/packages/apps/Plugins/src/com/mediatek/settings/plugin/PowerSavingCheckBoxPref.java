package com.mediatek.settings.plugin;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.mediatek.op09.plugin.R;

/**
 * Customization for CT,Power Saving Mode.
 * To control general items: CPU, WLAN, BT, GPS, Sync, Feedback
 */
public class PowerSavingCheckBoxPref extends CheckBoxPreference {

    private static final String TAG = "Power Savings";

    /**
     * Constructor method, add bew layout.
     * @param context Settings's context
     * @param attrs for parent's constructor
     */
    public PowerSavingCheckBoxPref(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.power_saving_checkbox_item);
        // setWidgetLayoutResource(R.layout.preference_inputmethod_widget);
        Log.d(TAG, "--PowerSavingCheckBoxPref  new attrs");
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Log.d(TAG, "--PowerSavingCheckBoxPref  onBindView");
        View poSavingItem = view.findViewById(R.id.power_saving_checkbox_pref);
        poSavingItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!isEnabled()) {
                    return;
                }
                setChecked(!isChecked());
                if (isChecked()) {
                    /** Turn off/on related functions through current preference key */
                    PowerSavingManager.getInstance(getContext()).turnOnPowerSavingMode(getKey());
                }
            }
        });
    }
}

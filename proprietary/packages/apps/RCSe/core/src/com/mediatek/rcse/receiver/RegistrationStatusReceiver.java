package com.mediatek.rcse.receiver;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.mediatek.rcse.activities.RegistrationStatusActivity;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * @author MTK33296 This Class used to receive broadcast when registration is
 *         failed first time on wifi.
 */
public class RegistrationStatusReceiver extends BroadcastReceiver {

    private Logger mLogger = Logger.getLogger(this.getClass().getName());

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mLogger.isActivated()) {
            mLogger.debug("Registration before config Broadcast Receievd");
        }
        try {
            Intent intentStatus = new Intent(context,
                    RegistrationStatusActivity.class);
            intentStatus.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentStatus);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            mLogger.debug(e.getMessage());
        }
    }
}

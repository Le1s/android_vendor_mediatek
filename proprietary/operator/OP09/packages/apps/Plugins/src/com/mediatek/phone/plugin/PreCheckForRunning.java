package com.mediatek.phone.plugin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mediatek.CellConnService.CellConnMgr;

/**
 * Use to unlock sim card.
 */
public class PreCheckForRunning {
    private CellConnMgr mCellConnMgr;
    private ServiceComplete mServiceComplete;
    private Context mContext;
    private Intent mIntent;
    private static final String TAG = "Settings/PreCheckForRunning";
    public boolean mByPass = false;

    /**
     * Use to unlock sim card.
     * @param ctx Context
     */
    public PreCheckForRunning(Context ctx) {
        mContext = ctx;
        mServiceComplete = new ServiceComplete();
        mCellConnMgr = new CellConnMgr(mServiceComplete);
        mCellConnMgr.register(mContext.getApplicationContext());
    }

    /**
     * Runnable for unlock.
     */
    class ServiceComplete implements Runnable {
        public void run() {
            int result = mCellConnMgr.getResult();
            Log.d(TAG, "ServiceComplete with the result = " + CellConnMgr.resultToString(result));
            if (CellConnMgr.RESULT_OK == result || CellConnMgr.RESULT_STATE_NORMAL == result) {
                mContext.startActivity(mIntent);
            }
        }
    }

    /**
     * check to run mCellConnMgr.
     * @param intent intent to startActivty
     * @param slotId sim id
     * @param req response id
     */
    public void checkToRun(Intent intent, int slotId, int req) {
        if (mByPass) {
            mContext.startActivity(intent);
            return ;
        }
        setIntent(intent);
        int r = mCellConnMgr.handleCellConn(slotId, req);
        Log.d(TAG, "The result of handleCellConn = " + CellConnMgr.resultToString(r));
    }

    public void setIntent(Intent it) {
        mIntent = it;
    }

    /**
     * when MultipleSimActivity Destory unregister it.
     */
    public void deRegister() {
        mCellConnMgr.unregister();
    }
}

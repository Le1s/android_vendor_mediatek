package com.hesine.nmsg.pn;


import android.content.Context;

import com.hesine.nmsg.api.PostPNToken;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.interfacee.Pipe;
import com.hesine.nmsg.util.MLog;
import com.hissage.hpe.SDK;

public class PNControler implements Pipe{
    /**
     * you have new email
     */
    public static final int COMMAND_NEW_MSG 			= 6666601;
    public static final int COMMAND_VERSION_UPGRADE 	= 6666602;
    public static final int COMMAND_UPLOAD_STATISTICS	= 6666603;
    
    private static final String TAG = "PNReceiver";

    public static void startPN(final Context context) {
        new Thread() {
            public void run() {
                // String regId = PNReceiver.readRegId(context);
                MLog.trace(TAG, "test entry PN");
                SDK.onRegister(context);
            };
        }.start();
    }
	
	private static PNControler ins = null;
	private static PNControler instance() {
		if(null == ins) {
			ins = new PNControler();
		}
		
		return ins;
	}
	
    public static void postPNToken() {
    	PostPNToken mPostPNToken = new PostPNToken();
    	mPostPNToken.setListener(PNControler.instance());
    	mPostPNToken.request();
    }
    
    public static void handlePNCommand(String message) {
    	PNMessageHandler.instance().handlePNCommand(message);
    }

	@Override
	public void complete(Object owner, Object data, int success) {
		if(Pipe.NET_SUCCESS == success) {
    		Config.saveUploadPNTokenFlag(true);
		} else {
			Config.saveUploadPNTokenFlag(false);
		}
	}
}
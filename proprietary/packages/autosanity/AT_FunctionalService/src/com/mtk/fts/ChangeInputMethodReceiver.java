package com.mtk.fts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.text.TextUtils;
import android.util.Log;
import android.provider.Settings;
//import android.content.ContentResolver;
//import android.content.pm.PackageManager;
//import android.inputmethodservice.InputMethodService;
//import android.view.inputmethod.InputMethodManager;
//import android.view.inputmethod.InputMethodSubtype;
//
//import java.util.List;
//
//
//import android.view.inputmethod.InputMethodInfo;


public class ChangeInputMethodReceiver extends BroadcastReceiver {
	public static final String ChangeInputMethod_intentAction = "com.mediatek.ChangeInputMethod";
	private static final String TAG = FTestService.TAG;
	@Override
	public void onReceive(Context context, Intent intent) {
		
         Log.i(TAG, "enter mtk ChangeInput receiver");
         Log.i(TAG, "Intent action = "+intent.getAction());
		 Settings.Secure.putString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD,"com.android.inputmethod.latin/.LatinIME");
//         final ContentResolver cr = context.getContentResolver();
//         
//         if(context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)!= 0){
//         	
//         	Log.i(TAG, "context requires permission "+android.Manifest.permission.WRITE_SECURE_SETTINGS);
//         	
//        }
//		     if(intent.getAction().equals(ChangeInputMethod_intentAction)){
//		     	
//		     	try{
//		     	
//		     	     String currentInputMethodId = Settings.Secure.getString(cr,
//                Settings.Secure.DEFAULT_INPUT_METHOD);
//                 Log.i(TAG, "pre imputMethod = "+currentInputMethodId);
//
//              /* Settings.Secure.putString(cr, Settings.Secure.DEFAULT_INPUT_METHOD,
//                    "com.android.inputmethod.latin/.LatinIME");*/
//              InputMethodManager mInputMManage = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//               Log.i(TAG, "the mInputMManager is not null ");
//              List<InputMethodInfo> InputMethods = mInputMManage.getInputMethodList();
//              String NewInputMethodName = InputMethods.get(0).getId();
//               Log.i(TAG, "the NewInputMethodName is : "+NewInputMethodName);
//              mInputMManage.setInputMethod(null, NewInputMethodName);
//              
//              
//                currentInputMethodId = Settings.Secure.getString(cr,
//                Settings.Secure.DEFAULT_INPUT_METHOD);
//                 Log.i(TAG, "after change imputMethod = "+currentInputMethodId);
//                 
//                }catch(Exception e){
//                	e.printStackTrace();
//                	
//                	
//                }
//                     
//              
//            }
		      

	}
//	public static CharSequence getCurrentInputMethodName(Context context, ContentResolver resolver,
//            InputMethodManager imm, List<InputMethodInfo> imis, PackageManager pm) {
//        if (resolver == null || imis == null) return null;
//        final String currentInputMethodId = Settings.Secure.getString(resolver,
//                Settings.Secure.DEFAULT_INPUT_METHOD);
//        if (currentInputMethodId.isEmpty()) return null;
//        for (InputMethodInfo imi : imis) {
//            if (currentInputMethodId.equals(imi.getId())) {
//                final InputMethodSubtype subtype = imm.getCurrentInputMethodSubtype();
//                final CharSequence imiLabel = imi.loadLabel(pm);
//                final CharSequence summary = subtype != null
//                        ? TextUtils.concat(subtype.getDisplayName(context,
//                                    imi.getPackageName(), imi.getServiceInfo().applicationInfo),
//                                            (TextUtils.isEmpty(imiLabel) ?
//                                                    "" : " - " + imiLabel))
//                        : imiLabel;
//                return summary;
//            }
//        }
//        return null;
//    }
}

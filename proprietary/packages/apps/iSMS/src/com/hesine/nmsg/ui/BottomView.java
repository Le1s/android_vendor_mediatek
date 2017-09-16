package com.hesine.nmsg.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hesine.hstat.common.EnumConstants;
import com.hesine.hstat.util.HstatDeviceInfo;
import com.hesine.nmsg.R;
import com.hesine.nmsg.activity.ConversationActivity;
import com.hesine.nmsg.config.Config;
import com.hesine.nmsg.util.DeviceInfo;

public class BottomView extends LinearLayout {
    public final static class msgType {
        public final static int moreAction = 0;
        public final static int sendMsg = 1;
        public final static int scrollToBottom = 2;
        public final static int hideMoreActionView = 3;
    }

//    private Context mContext;
    private View convertView;
    private ImageView mMore;
    private EditText mEdit;
    private ImageView mSend;
    private ActionListener mListener;
    private Context mContext;

    public BottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        convertView = LayoutInflater.from(context).inflate(R.layout.bottom_view, this, true);
        mMore = (ImageView) convertView.findViewById(R.id.more);
        mMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyBoard(false);
                mListener.doAction(msgType.moreAction);
            }
        });

        mEdit = (EditText) convertView.findViewById(R.id.edit);
        mEdit.clearFocus();
        mEdit.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEdit.onTouchEvent(event);
                //showKeyBoard(true);
                mListener.doAction(msgType.scrollToBottom);
                mListener.doAction(msgType.hideMoreActionView);
                return true;
            }
        });
        mEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				if(arg0.length() > 0){
					mSend.setImageResource(R.drawable.send_btn_selector);
					String value = arg0.toString();
					if(value.equalsIgnoreCase("*#NMSGDEBUG#")){
						nmsgInfoDialog();
					}
				}else{
					mSend.setImageResource(R.drawable.ic_send_disabled);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        mSend = (ImageView) convertView.findViewById(R.id.send);
        mSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!TextUtils.isEmpty(getContent())) {
                    showKeyBoard(false);
                    mListener.doAction(msgType.sendMsg);
                }
            }
        });
    }

    public void showKeyBoard(boolean flag) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (flag) {
            mEdit.requestFocus();
            imm.showSoftInput(mEdit, 0);
        } else {
            imm.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
        }
    }

    public void addActionListener(ActionListener listener) {
        mListener = listener;
    }

    public String getContent() {
        return mEdit.getEditableText().toString();
    }

    public String getContentForSend(){
        String content = mEdit.getEditableText().toString();
        mEdit.setText("");
        return content;
    }
    
	private void nmsgInfoDialog() {
		String appVersion = null;
		PackageManager manager = mContext.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(
					mContext.getPackageName(), 0);
			appVersion = info.versionName; // 版本名
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		new AlertDialog.Builder(mContext)
				.setItems(
						new String[] { "version:" + appVersion,
								"pn:" + Config.getPnToken(),
								"uuid:" + Config.getUuid(),
								"imsi:" + Config.getImsi(),
								"model:" + DeviceInfo.getDeviceModel(),
								"brand:" + DeviceInfo.getDeviceBrand(),
								"Hstat Id:" + HstatDeviceInfo.getDeviceId(mContext)}, null)
				.setNegativeButton(R.string.btn_ok, null).show();
	}
}

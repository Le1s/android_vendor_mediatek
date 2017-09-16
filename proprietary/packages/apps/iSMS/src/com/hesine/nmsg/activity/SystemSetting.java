package com.hesine.nmsg.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hesine.nmsg.R;
import com.hesine.nmsg.bean.ServiceInfo;
import com.hesine.nmsg.common.EnumConstants;
import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.statistics.statistics;
import com.hesine.nmsg.ui.ActionListener;
import com.hesine.nmsg.ui.CircularImage;
import com.hesine.nmsg.ui.HeaderView;
import com.hesine.nmsg.ui.PopupDialog;
import com.hesine.nmsg.util.MLog;

public class SystemSetting extends Activity {

	private TextView accountName;
	private TextView accountIntroduce;
	private TextView clearHistory;
	private CircularImage avatar;
	private ImageView accountSwitch;
	private HeaderView mHeader = null;
	private long threadId = 0;
	private ServiceInfo serviceInfo;
	private Bitmap icon;
	private boolean mIsChecked = true;
	private ProgressDialog mPDialog;
	private PopupDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting);
		initView();
		String account = getIntent().getExtras().getString(
				EnumConstants.NMSG_INTENT_EXTRA_ACCOUNT);
		threadId = getIntent().getExtras().getLong(
				EnumConstants.NMSG_INTENT_EXTRA_THREADID);
		if (account == null) {
			MLog.trace("SystemSetting", "user_account == null ");
			this.finish();
			return;
		}
		serviceInfo = DBUtils.getServiceInfo(account);
		if (serviceInfo == null) {
			MLog.trace("SystemSetting", "serviceInfo == null ");
			finish();
		}
		initHeader();
		setView();
	}

	private void initHeader() {
		mHeader = (HeaderView) findViewById(R.id.header);
		mHeader.setTitle(serviceInfo.getName());
		mHeader.getMoreView().setVisibility(View.GONE);
	}

	private void setView() {
		mIsChecked = serviceInfo.getStatus() > 0 ? true : false;
		accountName.setText(serviceInfo.getName());
		accountIntroduce.setText(serviceInfo.getDesc());
		accountSwitch.setImageResource(mIsChecked ? R.drawable.ic_on
				: R.drawable.ic_off);
		icon = BitmapFactory.decodeFile(serviceInfo.getIcon());
		if (icon != null) {
			avatar.setImageBitmap(icon);
		}
	}
	
    private static class MyHandler extends Handler {
        private final WeakReference<SystemSetting> mActivity;

        public MyHandler(SystemSetting activity) {
        	mActivity = new WeakReference<SystemSetting>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
        	SystemSetting activity = mActivity.get();
        	if (activity != null) {
        	  	activity.mPDialog.dismiss();
          	}
        }
    }

	public final Handler myHandler = new MyHandler(this);

    private void initView() {
        accountName = (TextView) findViewById(R.id.setting_account_name);
        accountIntroduce = (TextView) findViewById(R.id.setting_introduce_text);
        clearHistory = (TextView) findViewById(R.id.setting_clear_history);
        accountSwitch = (ImageView) findViewById(R.id.setting_account_switch);
        avatar = (CircularImage) findViewById(R.id.setting_account_avatar);

        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new PopupDialog(SystemSetting.this).setInfo(R.string.clear_all_messages)
                        .setLeft(R.string.btn_cancel).setRight(R.string.btn_ok).show();
                mDialog.setListener(new ActionListener() {
                    @Override
                    public void doAction(int type) {
                        if (type == PopupDialog.ActionType.ACTION_RIGHT) {
                            mPDialog = ProgressDialog.show(SystemSetting.this, null,
                                    SystemSetting.this.getString(R.string.delete_msg_wait_dialog),
                                    true, false);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if(DBUtils.deleteAllMsgViaThreadId(threadId) != -1){
                                        statistics.getInstance().threadsDelete(serviceInfo.getName(),statistics.threadDeleteType.clearMsg);
                                    }
                                    myHandler.sendEmptyMessage(0);
                                }
                            }).start();
                        }
                        mDialog.dismiss();
                    }
                });
            }
        });

        accountSwitch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mIsChecked = !mIsChecked;
                accountSwitch.setImageResource(mIsChecked ? R.drawable.ic_on : R.drawable.ic_off);
                DBUtils.updateServiceInfoStatus(serviceInfo.getAccount(), mIsChecked);// 0:off
                                                                                      // ,
                // 1: on
                statistics.getInstance().accountStatus(serviceInfo.getAccount(),
                        String.valueOf(mIsChecked ? 1 : 0));
            }
        });
    }

}

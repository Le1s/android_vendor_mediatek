package com.hesine.nmsg.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.widget.Toast;

import com.hesine.nmsg.R;
import com.hesine.nmsg.ui.RecordPopupWindow;
import com.hesine.nmsg.ui.Recorder;
import com.hesine.nmsg.ui.UiMessageId;
import com.hesine.nmsg.util.CommonUtils;
import com.hesine.nmsg.util.MLog;

@SuppressLint("SimpleDateFormat")
public class ConversationBaseActivity extends Activity implements Recorder.OnStateChangedListener {
    public static final String TAG = "ActionBaseActivity";

    public RecordBroadCast msgReceiver = null;
    public RecordBroadCast usbReceiver = null;
    public RecordPopupWindow popRecordView = null;
    public Vibrator vibrator;
    public Recorder mRecorder;
    public String mPicTempPath;
    public String mAudioTempPath;
    public String mVideoTempPath;
    public String mPhotoFilePath;
    public String mDstPhotoPath;
    

    private Runnable mUpdateVUMetur = new Runnable() {
        public void run() {
            updateVUMeterView();
        }
    };

    private Runnable updateTimer = new Runnable() {
        public void run() {
            updateTimerView();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addDataScheme("file");
        registerReceiver(usbReceiver, usbFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRecorder.state() == Recorder.PLAYING_STATE)
            mRecorder.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != usbReceiver) {
            try {
                unregisterReceiver(usbReceiver);
            } catch (Exception e) {
                MLog.trace(TAG, "This exceptiion is normal,because register in onResume");
            }
        }
    }

    private void init() {
        usbReceiver = new RecordBroadCast();
        mRecorder = new Recorder();
        mRecorder.setOnStateChangedListener(this);
        if (CommonUtils.getSDCardStatus()) {
            mPicTempPath = CommonUtils.getSDCardPath() + File.separator + "nmsg/attach"
                    + File.separator + "picture";
            File f = new File(mPicTempPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        if (CommonUtils.getSDCardStatus()) {
            mAudioTempPath = CommonUtils.getSDCardPath() + File.separator + "nmsg/attach"
                    + File.separator + "audio";
            File f = new File(mAudioTempPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        if (CommonUtils.getSDCardStatus()) {
            mVideoTempPath = CommonUtils.getSDCardPath() + File.separator + "nmsg/attach"
                    + File.separator + "video";
            File f = new File(mVideoTempPath);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    public void openImage(String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri uri = Uri.fromFile(new File(path));
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }


    public void playVideo(String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(path));
        intent.setDataAndType(uri, "video/*");
        startActivity(intent);
    }

//    public void doMoreAction(int type) {
//        switch (type) {
//        case ChatBottomMoreActionView.msgType.takePhoto:
//            takeCamera();
//            break;
//        case ChatBottomMoreActionView.msgType.imageVideo:
//            takePhotoAndVideo();
//            break;
//        case ChatBottomMoreActionView.msgType.location:
//            getLocation();
//            break;
//        case ChatBottomMoreActionView.msgType.recordVideo:
//            recordVideo();
//            break;
//        default:
//            break;
//        }
//    }

    public Handler handler = new Handler();

    public void takeCamera() {
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";
        mPhotoFilePath = mPicTempPath + File.separator + fileName;
        mDstPhotoPath = mPhotoFilePath;
        File out = new File(mPhotoFilePath);
        Uri uri = Uri.fromFile(out);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        try {
            startActivityForResult(imageCaptureIntent, UiMessageId.NMS_IM_TAKE_CAMERA);
        } catch (Exception e) {
            Toast.makeText(this, R.string.chat_no_app, Toast.LENGTH_SHORT).show();
            MLog.trace(TAG, MLog.GetStactTrace(e));
        }
    }

    public void takePhoto() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        mDstPhotoPath = mPicTempPath + File.separator + fileName + ".jpg";
        try {
            startActivityForResult(intent, UiMessageId.NMS_IM_TAKE_PHOTO);
        } catch (Exception e) {
            Toast.makeText(this, R.string.chat_no_app, Toast.LENGTH_SHORT).show();
            MLog.trace(TAG, MLog.GetStactTrace(e));
        }
    }

    public void makeAudio() {
    }

    public void getLocation() {

    }

    public void closeMoreAction() {

    }

    public String audioPath;

    public void startRecord() {
        audioPath = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        popRecordView.startRecordAndShowWindow();
        mRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB, mAudioTempPath + File.separator
                + audioPath + ".amr");
    }
    public void updateRecordReleaseToSend(){
        popRecordView.setReleaseToSend();
    }
    public void updateRecordMotionUpToRelease(){
    	popRecordView.setMotionUpToCancel();
    }
    
    public void updateRecordFingerUpToRelease(){
    	popRecordView.setReleaseToCancel();
    }
    
    public void cancelRecord(){
        mRecorder.cancel();
        popRecordView.dissWindow();
    }
    
    public void stopRecord() {
        mRecorder.stop();
        popRecordView.dissWindow();
    }

    private int mPreviousVUMax = 0;

    private void updateVUMeterView() {
    	if(true)
    		return;
        @SuppressWarnings("unused")
		final int MAX_VU_SIZE = 6;
        boolean showVUArray[] = new boolean[MAX_VU_SIZE];

        if (popRecordView.isShow() && mRecorder.state() == Recorder.RECORDING_STATE) {
            int vuSize = MAX_VU_SIZE * mRecorder.getMaxAmplitude() / 32768;
            if (vuSize >= MAX_VU_SIZE) {
                vuSize = MAX_VU_SIZE - 1;
            }

            if (vuSize >= mPreviousVUMax) {
                mPreviousVUMax = vuSize;
            } else if (mPreviousVUMax > 0) {
                mPreviousVUMax--;
            }

            for (int i = 0; i < MAX_VU_SIZE; i++) {
                if (i <= vuSize) {
                    showVUArray[i] = true;
                } else if (i == mPreviousVUMax) {
                    showVUArray[i] = true;
                } else {
                    showVUArray[i] = false;
                }
            }

            handler.postDelayed(mUpdateVUMetur, 100);
        } else if (popRecordView.isShow()) {
            mPreviousVUMax = 0;
            for (int i = 0; i < MAX_VU_SIZE; i++) {
                showVUArray[i] = false;
            }
        }

        int i = 0;
        if (popRecordView.isShow()) {
            for (boolean show : showVUArray) {
                if (show) {
                    popRecordView.setVoice(i);
                }
                i++;
            }
        }
    }

    private void updateTimerView() {
        int state = mRecorder.state();

        boolean ongoing = state == Recorder.RECORDING_STATE || state == Recorder.PLAYING_STATE;

        long time = mRecorder.progress();
        String timeStr = time + "";
        if (time == 60) {
            mRecorder.stop();
        }
        timeStr = String.format(getString(R.string.chat_make_audio_format), timeStr);
        if (popRecordView.isShow()) {
            popRecordView.setTime(timeStr);
        }

        if (ongoing) {
            handler.postDelayed(updateTimer, 100);
        }
    }

    public void updateUi() {
        updateTimerView();
        updateVUMeterView();
    }

    @Override
    public void onStateChanged(int state) {
        if (state == Recorder.IDLE_STATE) {
            popRecordView.dissWindow();
        } else if (state == Recorder.RECORDING_STATE) {
            popRecordView.showWindow();
            updateUi();
        }
    }

    @Override
    public void onError(int error) {

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mRecorder.state() == Recorder.PLAYING_STATE)
                mRecorder.stop();
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    class RecordBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MLog.trace(TAG, "get broadcase action is: " + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                init();
            }
        }
    }
}
/*
 * MediaTek (Author : Chien-Lin Huang)
 * Android sample codes for voice interface extension, VIE SDK
 */
package com.example.mediatek.voiceextension;

import java.io.File;
import java.io.FileNotFoundException;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//import VIE SDK components
import com.mediatek.voiceextension.VoiceCommandManager;
import com.mediatek.voiceextension.VoiceCommandListener;
import com.mediatek.voiceextension.VoiceCommandResult;


public class MainActivity extends ActionBarActivity {
    static Button btnCreate_ = null;
    static EditText edtInfo_ = null;
    boolean bCheck = false, bInit = true;

    // create VIE objects
    VoiceCommandManager vcm_ = null;
    ResponseListener listener_ = null;

    // define VIE return codes
    public static final int VIE_API_COMMAND_IDLE = 1;
    public static final int API_COMMAND_START_RECOGNITION = VIE_API_COMMAND_IDLE + 1;
    public static final int API_COMMAND_STOP_RECOGNITION = VIE_API_COMMAND_IDLE + 2;
    public static final int API_COMMAND_PAUSE_RECOGNITION = VIE_API_COMMAND_IDLE + 3;
    public static final int API_COMMAND_RESUME_RECOGNITION = VIE_API_COMMAND_IDLE + 4;
    public static final int API_COMMAND_RECOGNIZE_RESULT = VIE_API_COMMAND_IDLE + 5;
    public static final int API_COMMAND_NOTIFY_ERROR = VIE_API_COMMAND_IDLE + 6;
    public static final int API_COMMAND_SET_COMMANDS = VIE_API_COMMAND_IDLE + 7;

    /**
     * define VIE return codes
     */
    private String retCodeDes_(int retCode)
    {
        if (retCode == VoiceCommandResult.SUCCESS) { return "SUCCESS"; }
        else if (retCode == VoiceCommandResult.WRITE_STORAGE_FAIL) { return "WRITE_STORAGE_FAIL"; }
        else if (retCode == VoiceCommandResult.MIC_INIT_FAIL) { return "MIC_INIT_FAIL"; }
        else if (retCode == VoiceCommandResult.MIC_OCCUPIED) { return "MIC_OCCUPIED"; }
        else if (retCode == VoiceCommandResult.LISTENER_ILLEGAL) { return "LISTENER_ILLEGAL"; }
        else if (retCode == VoiceCommandResult.LISTENER_ALREADY_SET) { return "LISTENER_ALREADY_SET"; }
        else if (retCode == VoiceCommandResult.RECOGNITION_NEVER_START) { return "RECOGNITION_NEVER_START"; }
        else if (retCode == VoiceCommandResult.RECOGNITION_NEVER_PAUSE) { return "RECOGNITION_NEVER_PAUSE"; }
        else if (retCode == VoiceCommandResult.RECOGNITION_ALREADY_STARTED) { return "RECOGNITION_ALREADY_STARTED"; }
        else if (retCode == VoiceCommandResult.RECOGNITION_ALREADY_PAUSED) { return "RECOGNITION_ALREADY_PAUSED"; }
        else if (retCode == VoiceCommandResult.SERVICE_NOT_EXIST) { return "SERVICE_NOT_EXIST"; }
        else if (retCode == VoiceCommandResult.SERVICE_DISCONNECTTED) { return "SERVICE_DISCONNECTTED"; }
        else if (retCode == VoiceCommandResult.PROCESS_ILLEGAL) { return "PROCESS_ILLEGAL"; }
        else if (retCode == VoiceCommandResult.COMMANDSET_ALREADY_EXIST) { return "COMMANDSET_ALREADY_EXIST"; }
        else if (retCode == VoiceCommandResult.COMMANDSET_NOT_EXIST) { return "COMMANDSET_NOT_EXIST"; }
        else if (retCode == VoiceCommandResult.COMMANDSET_NAME_ILLEGAL) { return "COMMANDSET_NAME_ILLEGAL"; }
        else if (retCode == VoiceCommandResult.COMMANDS_NUM_EXCEED_LIMIT) { return "COMMANDS_NUM_EXCEED_LIMIT"; }
        else if (retCode == VoiceCommandResult.COMMANDSET_ALREADY_SELECTED) { return "COMMANDSET_ALREADY_SELECTED"; }
        else if (retCode == VoiceCommandResult.COMMANDSET_NOT_SELECTED) { return "COMMANDSET_NOT_SELECTED"; }
        else if (retCode == VoiceCommandResult.COMMANDSET_OCCUPIED) { return "COMMANDSET_OCCUPIED"; }
        else if (retCode == VoiceCommandResult.COMMANDS_DATA_INVALID) { return "COMMANDS_DATA_INVALID"; }
        else if (retCode == VoiceCommandResult.COMMANDS_FILE_ILLEGAL) { return "COMMANDS_FILE_ILLEGAL"; }
        else if (retCode == VoiceCommandResult.COMMANDS_NUM_EXCEED_LIMIT) { return "COMMANDS_NUM_EXCEED_LIMIT"; }
        else { return "???"; }
    }

    /**
     * define the response to "Start Recognition"
     */
    private void responseToStart_(int retCode)
    {
        if (retCode != VoiceCommandResult.SUCCESS) { return; }
    }

    /**
     * define the response to "Stop Recognition"
     */
    private void responseToStop_(int retCode)
    {
        if (retCode != VoiceCommandResult.SUCCESS) { return; }
    }

    /**
     * define the response to "Pause Recognition"
     */
    private void responseToPause_(int retCode)
    {
        if (retCode != VoiceCommandResult.SUCCESS) { return; }
    }

    /**
     * define the response to "Resume Recognition"
     */
    private void responseToResume_(int retCode)
    {
        if (retCode != VoiceCommandResult.SUCCESS) { return; }
    }

    /**
     * define the response to "Set Commands"
     */
    private void responseToSetCommands_(int retCode)
    {
        if (retCode != VoiceCommandResult.SUCCESS) { return; }
        getcmd();
    }

    /**
     * define the relation between the return code and the response
     */
    public Handler handler_ = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            int retCode = b.getInt("retCode");

            switch (msg.what) {
            case API_COMMAND_SET_COMMANDS:
                responseToSetCommands_(retCode);
                break;

            case API_COMMAND_RECOGNIZE_RESULT:
                String commandStr = b.getString("commandStr");
                edtInfo_.append("Recognition : " + commandStr + "\n");
                break;

            case API_COMMAND_NOTIFY_ERROR:
                edtInfo_.append(retCodeDes_(retCode) + "\n");
                break;

            case API_COMMAND_START_RECOGNITION:
                responseToStart_(retCode);
                break;

            case API_COMMAND_STOP_RECOGNITION:
                responseToStop_(retCode);
                break;

            case API_COMMAND_PAUSE_RECOGNITION:
                responseToPause_(retCode);
                break;

            case API_COMMAND_RESUME_RECOGNITION:
                responseToResume_(retCode);
                break;

            default:
                break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreate_ = (Button) findViewById(R.id.btnCreate);
        edtInfo_ = (EditText) findViewById(R.id.edtInfo);

        // VIE: get instance
        vcm_ = VoiceCommandManager.getInstance();
        if (vcm_ == null)
        { edtInfo_.append("VIE SDK not found\n"); }
        // VIE: create a listener
        listener_ = new ResponseListener();

        if (bInit) {
            // VIE: select commands
            String setName = "camera";
            vcm_.selectCurrentCommandSet(setName, listener_);
            int retCode = vcm_.isCommandSetCreated(setName);
            // VIE: check command setting
            if (retCode == VoiceCommandResult.SUCCESS) {
                getcmd();
            } else {
                // VIE: create commands
                vcm_.createCommandSet(setName);
                // VIE: select commands
                vcm_.selectCurrentCommandSet(setName, listener_);
                // VIE: set commands
                edtInfo_.append("\nPlease wait for setting commands\n");
                File file = new File("system/etc/voicecommand/cmd_camera.xmf");
                try {
                    vcm_.setCommands(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            bInit = false;
        }

        btnCreate_.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (bCheck) {
                    // VIE: stop recognition
                    try {
                        vcm_.stopRecognition();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    btnCreate_.setText("Start");
                    bCheck = false;
                } else {
                    // VIE: start recognition
                    try {
                        vcm_.startRecognition();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    btnCreate_.setText("Stop");
                    bCheck = true;
                }
            } });

    }

    /**
     * get available commands
     */
    private void getcmd()
    {
        edtInfo_.append("\nPlease speak words: ");
        String[] commands = null;
        try {
            commands = vcm_.getCommands();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < commands.length; ++i)
        {
            edtInfo_.append(commands[i]);
            if (i < (commands.length - 1))
            { edtInfo_.append(", "); }
        }
        edtInfo_.append("\n");
        btnCreate_.setEnabled(true);
    }

    /**
     * send return codes
     */
    private void sendRetCode_(int api, int retCode)
    {
        Message m = new Message();
        Bundle b = new Bundle();
        b.putInt("retCode", retCode);

        m.what = api;
        m.setData(b);
        handler_.sendMessage(m);
    }

    /**
     * VIE response listener
     */
    class ResponseListener extends VoiceCommandListener
    {
        public void onCommandRecognized(int retCode , int commandId , String commandStr)
        {
            Message m = new Message();
            Bundle b = new Bundle();
            b.putInt("retCode", retCode);
            b.putInt("commandId", commandId);
            b.putString("commandStr", commandStr);

            m.what = API_COMMAND_RECOGNIZE_RESULT;
            m.setData(b);
            handler_.sendMessage(m);
        }

        public void onSetCommands(int retCode)
        { sendRetCode_(API_COMMAND_SET_COMMANDS, retCode); }

        public void onStartRecognition(int retCode)
        { sendRetCode_(API_COMMAND_START_RECOGNITION, retCode); }

        public void onStopRecognition(int retCode)
        { sendRetCode_(API_COMMAND_STOP_RECOGNITION, retCode); }

        public void onError(int retCode)
        { sendRetCode_(API_COMMAND_NOTIFY_ERROR, retCode); }

        public void onPauseRecognition(int retCode)
        { sendRetCode_(API_COMMAND_PAUSE_RECOGNITION, retCode); }

        public void onResumeRecognition(int retCode)
        { sendRetCode_(API_COMMAND_RESUME_RECOGNITION, retCode); }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

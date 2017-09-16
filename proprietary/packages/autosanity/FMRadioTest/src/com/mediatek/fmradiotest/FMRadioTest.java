package com.mediatek.fmradiotest;


import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.Activity;
import android.media.AudioManager;
import android.content.Context;
import com.jayway.android.robotium.solo.Solo;
import com.mediatek.fmradio.FmRadioActivity;
/**
 * @author MTK54140
 */
public class FMRadioTest extends
        ActivityInstrumentationTestCase2<FmRadioActivity> {
    private Activity mActivity = null;
    private AudioManager mAM = null;
    private ImageButton mNextButton;
    private Solo solo;
    private Context mContext;
    final String PACKAGE_UNDER_TEST_FMRADIO = "com.mediatek.fmradio";
    private static final String TAG = "FMRadioTest";
    private int id;
    private int id2;
    private int id3;
    private int id_frequency;
    private View Menu;
    private String Search;
    private View NextStation;
    private View mStationValue;
    /**
     * constructor
     */
    public FMRadioTest() {
        super(FmRadioActivity.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        solo = new Solo(getInstrumentation(), getActivity());
        mActivity = getActivity();
        mAM = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
/*      id = mActivity.getResources().getIdentifier("fm_menu", "id", PACKAGE_UNDER_TEST_FMRADIO);
        Menu = solo.getView(id);
        id2 = mActivity.getResources().getIdentifier("optmenu_search", "string", PACKAGE_UNDER_TEST_FMRADIO);
        Search = mContext.getString(id2);*/
        id3 = mActivity.getResources().getIdentifier("button_nextstation", "id", PACKAGE_UNDER_TEST_FMRADIO);
        id_frequency = mActivity.getResources().getIdentifier("station_value", "id", PACKAGE_UNDER_TEST_FMRADIO);
        NextStation = solo.getView(id3);
        mStationValue = solo.getView(id_frequency);
        Log.v(TAG, "mAM is " + mAM);

    }

    public void tearDown() throws Exception {
        //solo.clickOnMenuItem("Exit");
        getActivity().finish();
        super.tearDown();
        SystemClock.sleep(2000);
    }
   public String getChannel() {
//      TextView Channel = solo.getText(6);
       TextView Channel = (TextView) mStationValue;
        String sChannel = Channel.getText().toString();
        Log.v(TAG, "getChannel channel=" + sChannel);
        return sChannel;
   }
    /***
     * @throws Throwable
     */
    public void test_fmradio() throws Exception {

        final String TEST_NAME = "test_fmradio";
        final String MESSAGE = "[" + TEST_NAME + "] ";
            Log.v(TAG, MESSAGE + "test start");
            SystemClock.sleep(3000);
            // search channel
            /*solo.clickOnView(Menu);
            SystemClock.sleep(1000);
            solo.clickOnText(Search);
            SystemClock.sleep(1000);
            solo.waitForDialogToClose(60000);
            solo.goBack();
            SystemClock.sleep(1000);*/
            String Channel = getChannel();
//          assertTrue(MESSAGE + "FMRadio can not be played after search", mAM.isFmActive());
            //Play next channel
            //solo.clickOnImageButton(8);
            solo.clickOnView(NextStation);
            SystemClock.sleep(15000);
            String NewChannel = getChannel();
//          assertTrue(MESSAGE + "FMRadio can not play next channel", mAM.isFmActive());
            assertNotSame(Channel, NewChannel);
            Log.v(TAG, MESSAGE + "test end");
    }

}

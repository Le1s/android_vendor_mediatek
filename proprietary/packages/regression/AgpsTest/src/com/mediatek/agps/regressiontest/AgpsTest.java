/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2011. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.agps.regressiontest;

import android.util.Log;
import android.test.InstrumentationTestCase;
import android.content.Context;

import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.location.LocationManager;
import android.location.GpsStatus.NmeaListener;
import android.location.GpsStatus;
import android.location.GpsSatellite;
import android.location.LocationListener;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import java.io.IOException;
import com.mediatek.lbs.em2.utils.AgpsInterface;

import android.os.SystemClock;
//import android.widget.Toast;

public class AgpsTest extends InstrumentationTestCase {

    private static final String TAG = "agps_test";
    private static final boolean FUNCTION_LOG = true;
    private static final boolean DEBUG_LOG = false;
    private static final boolean ALLOW_ADVANCED_TC = true;

    private static final String  TargetSlp     = "supl.google.com";
    private static final int     TargetSlpPort = 7276;
    private static final boolean TargetSlpTls  = false;

    public final static int WORKER_CMD_GPS_START = 1;
    public final static int WORKER_CMD_GPS_STOP  = 2;

    private static boolean sInited;

    private static AgpsInterface sAgpsInf;
    private static LocationManager sLocMgr;

    private static boolean sTest01Result;
    private static boolean sTest02Result;
    private static boolean sTest03Result;
    private static boolean sTest04Result;
    private static boolean sTest05Result;
    private static boolean sTest06Result;
    private static boolean sTest07Result;
    private static boolean sTest08Result;
    private static boolean sTest09Result;
//  private static boolean sTest10Result;
    private static boolean sHasSnr;
    private static boolean sIfFirstTest = true;

    private Context mContext;

    private boolean mHasNmea;
    private boolean mHasStatus;
    private boolean mHasSnr;
    private boolean mHasFix;
    private Object mWaitNmea;
    private Object mWaitStatus;
    private Object mWaitSnr;
    private Object mWaitFix;
    private int    mNmeaTimeout;
    private int    mStatusTimeout;
    private int    mSnrTimeout;
    private int    mFixTimeout;
    private HandlerThread mHandlerThread;
    private ListenerHandler mListenerHandler;
    private long   mGpsStartTS;
    private long   mGotFixTs;
    private long   mPeriodToFix;
    private double mLat;
    private double mLng;

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    private void function_log(String msg) {
        if (FUNCTION_LOG) log(msg);
    }

    private void msleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (DEBUG_LOG) function_log("setUp()+");
        mContext = getInstrumentation().getTargetContext();
        myInit();
        if (DEBUG_LOG) function_log("setUp()-");
    }

    @Override
    protected void tearDown() throws Exception {
        if (DEBUG_LOG) function_log("tearDown()+");
        mContext = null;
        myDeInit();
        if (DEBUG_LOG) function_log("tearDown()-");
        super.tearDown();
    }

    public void initTimeout(boolean bUseAgps) {
        mNmeaTimeout   =  5000;
        if (bUseAgps) {
            mStatusTimeout = 10000;
            mSnrTimeout    = 15000;
            mFixTimeout    = 20000;
        } else {
            mStatusTimeout = 30000;
            mSnrTimeout    = 40000;
            mFixTimeout    = 60000;
        }
    }

    public void myInit() throws IOException, SettingNotFoundException {
        if (!sInited) {
            sInited = true;

            sLocMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            sAgpsInf = new AgpsInterface();

            sAgpsInf.setSuplProfile(TargetSlp, TargetSlpPort, TargetSlpTls);

            //// using old API, level 8
            //Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(), LocationManager.GPS_PROVIDER, true);
            // using new API, level 19
            if (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
                Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
                msleep(3000);
            }
        }
        mWaitNmea   = new Object();
        mWaitStatus = new Object();
        mWaitSnr    = new Object();
        mWaitFix    = new Object();
        initTimeout(true);

        mHandlerThread = new HandlerThread("[agps]");
        mHandlerThread.start();
        mListenerHandler = new ListenerHandler(mHandlerThread.getLooper());
    }

    public void myDeInit() {
        // TBD
    }

    protected enum GpsResetMode {
        Hot, Warm, Cold, Full,
    }

    protected NmeaListener mNmaeListener = new NmeaListener() {
        public void onNmeaReceived(long timestamp, String nmeaStr) {
            if (mHasNmea == true) {
                return;
            }
            if (DEBUG_LOG) log("nmea: " + nmeaStr);
            if (DEBUG_LOG) function_log("mNmaeListener()+");
            synchronized (mWaitNmea) {
              mHasNmea = true;
              log("mHasNmea = true");
              mWaitNmea.notify();
            }
            if (DEBUG_LOG) function_log("mNmaeListener()-");
        }
    };

    protected GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int gpsEvent) {
            if ((gpsEvent != GpsStatus.GPS_EVENT_SATELLITE_STATUS) ||
                (mHasStatus && !sIfFirstTest && !sHasSnr) ||
                mHasSnr) {
                if (DEBUG_LOG) log("onGpsStatusChanged(): gpsEvent=" + gpsEvent + ", mHasSnr" + mHasSnr + ", status=" + mHasStatus + ", sIfFirstTest=" + sIfFirstTest + ", sHasSnr=" + sHasSnr);
                return;
            }
            if (DEBUG_LOG) function_log("onGpsStatusChanged()+");
            int count  = 0;
            int count2 = 0;
            GpsStatus status = sLocMgr.getGpsStatus(null);

            Iterable<GpsSatellite> gpsSat = status.getSatellites();
            for (GpsSatellite sat : gpsSat) {
                if (sat.getAzimuth() != 0 || sat.getElevation() != 0) {
                    if (DEBUG_LOG) log("PRN: " + sat.getPrn() + " Azimuth:" + sat.getAzimuth() + " Elevation:" + sat.getElevation() + " Snr:" + sat.getSnr());
                    count++;
                    if (sat.getSnr() > 22) {
                        count2++;
                    }
                }
            }
            if (!mHasStatus && count > 3) {
                synchronized (mWaitStatus) {
                    mHasStatus = true;
                    log("mHasStatus = true");
                    mWaitStatus.notify();
                }
            }
            if (!mHasSnr && count2 > 3) {
                synchronized (mWaitSnr) {
                    mHasSnr = true;
                    sHasSnr = true;
                    log("mHasSnr = true");
                    mWaitSnr.notify();
                }
            }
            if (DEBUG_LOG) function_log("onGpsStatusChanged()-");
        }
    };

    protected LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            function_log("onLocationChanged()+");
            if (location == null) {
                log("ERR: location is null");
            } else {
                if (!mHasFix) {
                    synchronized (mWaitFix) {
                        mHasFix = true;
                        sHasSnr = true;
                        mGotFixTs = getTick();
                        mLat = location.getLatitude();
                        mLng = location.getLongitude();
                        mWaitFix.notify();
                    }
                }
            }

            function_log("onLocationChanged()-");
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    protected boolean waitNmea() {
        synchronized (mWaitNmea) {
            try {
                mWaitNmea.wait(mNmeaTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mHasNmea;
    }

    protected boolean waitStatus() {
        synchronized (mWaitStatus) {
            try {
                mWaitStatus.wait(mStatusTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mHasStatus;
    }

    protected boolean waitSnr() {
        synchronized (mWaitSnr) {
            try {
                mWaitSnr.wait(mSnrTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mHasSnr;
    }

    protected boolean waitFix() {
        synchronized (mWaitFix) {
            try {
                mWaitFix.wait(mFixTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mHasFix;
    }

    protected void gpsReset(GpsResetMode mode) {
        Bundle extras = new Bundle();
        if (DEBUG_LOG) log("Gps Reset Mode: " + mode);
        switch (mode) {
        case Cold:
            extras.putBoolean("ephemeris", true);
            extras.putBoolean("position", true);
            extras.putBoolean("time", true);
            extras.putBoolean("iono", true);
            extras.putBoolean("utc", true);
            extras.putBoolean("health", true);
            break;
        case Full:
            extras.putBoolean("all", true);
            break;
        case Hot:
            extras.putBoolean("rti", true);
            break;
        case Warm:
            extras.putBoolean("ephemeris", true);
            break;
        default:
            log("ERR: Unknown mode=" + mode);
            break;

        }
        sLocMgr.sendExtraCommand(
            LocationManager.GPS_PROVIDER,
            "delete_aiding_data",
            extras);
        msleep(500);
    }

    protected void gpsStart() {
        sLocMgr.addNmeaListener(mNmaeListener);
        sLocMgr.addGpsStatusListener(mGpsStatusListener);
        mGpsStartTS = getTick();
        sLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                0, mLocationListener);
    }

    protected void gpsStop() {
        sLocMgr.removeNmeaListener(mNmaeListener);
        sLocMgr.removeGpsStatusListener(mGpsStatusListener);
        sLocMgr.removeUpdates(mLocationListener);
    }

    protected void askWorkerThreadDoGpsStart() {
        Message m = Message.obtain();
        m.what = WORKER_CMD_GPS_START;
        mListenerHandler.sendMessage(m);
    }

    protected void askWorkerThreadDoGpsStop() {
        Message m = Message.obtain();
        m.what = WORKER_CMD_GPS_STOP;
        mListenerHandler.sendMessage(m);
    }

    protected class ListenerHandler extends Handler {
        public ListenerHandler(Looper l) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
            // Bundle bundle = msg.getData();
            int what = msg.what;
            switch (what) {
            case WORKER_CMD_GPS_START:
                gpsStart();
                break;
            case WORKER_CMD_GPS_STOP:
                gpsStop();
                break;
            default:
                log("ERR: unsupport what=" + what);
                break;
            }
        }
    }

    private long getTick() {
        return SystemClock.elapsedRealtime();
    }

    private void toast(String msg) {
        //Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

    // test the precondition
    public void test01_Precondition() throws SettingNotFoundException {
        function_log("test01_Precondition()+");
        assertNotNull(sAgpsInf);
        assertNotNull(sLocMgr);
        assertNotNull(mContext);
        assertNotNull(mWaitNmea);
        assertNotNull(mWaitStatus);
        assertNotNull(mNmaeListener);
        assertNotNull(mGpsStatusListener);
        assertNotNull(mLocationListener);
        assertNotNull(mHandlerThread);
        assertNotNull(mListenerHandler);
        assertEquals(Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE), Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
        function_log("test01_Precondition()-: OK");
        //toast("test01_Precondition(): OK");
        sTest01Result = true;
    }

    public void test02_Precondition_SocketToSLP() {
        function_log("test02_Precondition_SocketToSLP()+");
        int result = 0;

        try {
            Socket socket = new Socket();
            InetSocketAddress isa = new InetSocketAddress(TargetSlp, TargetSlpPort);
            socket.connect(isa, 10 * 1000);
            socket.close();
            result = 1;
        } catch (UnknownHostException e) {
            result = 2;
        } catch (IOException e) {
            result = 3;
        }
        function_log("test02_Precondition_SocketToSLP()-: " + ((result == 1) ? "OK" : "FAILURE (Please check your data connection or WIFI settings)"));
        //toast("test02_Precondition_SocketToSLP(): " + ((result==1)?"OK":"FAILURE"));
        assertEquals(result, 1);
        sTest02Result = true;
    }

    public void test03_Gps() {
        boolean ret = false;
        function_log("test03_Gps()+");
        sAgpsInf.setAgpsEnabled(false);
        initTimeout(false);
        msleep(500);

        askWorkerThreadDoGpsStart();

        ret = waitNmea();

        askWorkerThreadDoGpsStop();
        msleep(500);
        function_log("test03_Gps()-: " + (ret ? "OK" : "FAILURE"));
        //toast("test03_Gps(): " + (ret?"OK":"FAILURE"));
        assertTrue(ret);
        sTest03Result = true;
    }

    public boolean doAgpsTest(GpsResetMode mode, boolean bUseAgps, String title) {
        boolean ret = false;
        mHasNmea   = false;
        mHasStatus = false;
        mHasSnr    = false;
        mHasFix    = false;

        function_log(title + "+");

        sAgpsInf.setAgpsEnabled(bUseAgps);
        initTimeout(bUseAgps);
        gpsReset(mode);
        msleep(500);

        askWorkerThreadDoGpsStart();

        if (sIfFirstTest || sHasSnr) {
            // GPS/AGPS Test with satellite signals
            waitFix();
        } else if (bUseAgps) {
            // AGPS Test without satellite signals
            waitStatus();
        } else {
            // GPS Test without satellite signals
            waitNmea();
        }

        askWorkerThreadDoGpsStop();
        msleep(500);

        if (sHasSnr) {
            // GPS/AGPS Test with satellite signals
            if (mHasFix) {
                mPeriodToFix = mGotFixTs - mGpsStartTS;
                log("TimeToFix=" + mPeriodToFix + ", lat=" + mLat + ", lng=" + mLng);
                if (mPeriodToFix > mFixTimeout) {
                    log("Time to fix is too high");
                } else {
                    ret = true;
                }
            } else {
                log("Timeout to wait for location report");
            }
        } else if (bUseAgps) {
            // Assume AGPS Test without satellite signals
            if (mHasStatus) {
                ret = true;
            } else {
                log("Timeout to wait for satellite info");
            }
        } else {
            // Assume GPS Test without satellite signals
            if (mHasNmea) {
                ret = true;
            } else {
                log("Timeout to wait for satellite info");
            }
        }
        sIfFirstTest = false;

        function_log(title + "-: " + (ret ? "OK" : "FAILURE"));
        //toast(title + ": " + (ret?"OK":"FAILURE"));
        return ret;
    }

    public void test04_GpsFull() {
        boolean ret = false;

        if (!sTest01Result || !sTest03Result) {
            function_log("test04_GpsFull(): skipped");
            //toast("test04_GpsFull(): skipped");
            return;
        }

        ret = doAgpsTest(GpsResetMode.Full, false, "test04_GpsFull()");

        assertTrue(ret);
        sTest04Result = true;
    }

    public void test05_GpsCold() {
        boolean ret = false;

        if (//!ALLOW_ADVANCED_TC ||
            !sTest01Result || !sTest03Result) {
            function_log("test05_GpsCold(): skipped");
            //toast("test05_GpsCold(): skipped");
            return;
        }

        ret = doAgpsTest(GpsResetMode.Cold, false, "test05_GpsCold()");

        assertTrue(ret);
        sTest05Result = true;
    }

    public void test06_GpsWarm() {
        boolean ret = false;

        if (//!ALLOW_ADVANCED_TC ||
            !sTest01Result || !sTest03Result) {
            function_log("test06_GpsWarm(): skipped");
            //toast("test06_GpsWarm(): skipped");
            return;
        }

        ret = doAgpsTest(GpsResetMode.Warm, false, "test06_GpsWarm()");

        assertTrue(ret);
        sTest06Result = true;
    }

    public void test07_GpsHot() {
        boolean ret = false;

        if (//!ALLOW_ADVANCED_TC ||
            !sTest01Result || !sTest03Result) {
            function_log("test07_GpsHot(): skipped");
            //toast("test07_GpsHot(): skipped");
            return;
        }

        ret = doAgpsTest(GpsResetMode.Hot, false, "test07_GpsHot()");

        assertTrue(ret);
        sTest07Result = true;
    }

    public void test08_AgpsFull() {
        boolean ret = false;

        if (!sTest01Result || !sTest02Result || !sTest03Result) {
            function_log("test08_AgpsFull(): skipped");
            //toast("test08_AgpsFull(): skipped");
            return;
        }

        ret = doAgpsTest(GpsResetMode.Full, true, "test08_AgpsFull()");

        assertTrue(ret);
        sTest08Result = true;
    }

    public int doStress(boolean bUseAgps, String title) {
        boolean ret = false;
        int nTotal  = 10;
        int nFailFull  = 0;
        int nFailCold  = 0;
        int nFailWarm  = 0;
        int nFailHot   = 0;
        int nFailTotal = 0;
        int i;

        if (!ALLOW_ADVANCED_TC ||
            !sTest01Result || (bUseAgps && !sTest02Result) || !sTest03Result || !sTest04Result ||
            !sTest05Result || !sTest06Result || !sTest07Result || (bUseAgps && !sTest08Result)) {
            function_log(title + "(): skipped");
            //toast(title + "(): skipped");
            return 0;
        }

        function_log(title + "()+");

        for (i = 0; i < nTotal; i++) {
            ret = doAgpsTest(GpsResetMode.Full, bUseAgps, title + "_Full(" + i + ")");
            if (!ret) {
                nFailFull++;
                nFailTotal++;
            }
            ret = doAgpsTest(GpsResetMode.Cold, bUseAgps, title + "_Cold(" + i + ")");
            if (!ret) {
                nFailCold++;
                nFailTotal++;
            }
            ret = doAgpsTest(GpsResetMode.Warm, bUseAgps, title + "_Warm(" + i + ")");
            if (!ret) {
                nFailWarm++;
                nFailTotal++;
            }
            ret = doAgpsTest(GpsResetMode.Hot, bUseAgps,  title + "_Hot(" + i + ")");
            if (!ret) {
                nFailHot++;
                nFailTotal++;
            }
        }

        log(title + "_Full(): Pass=" + (nTotal - nFailFull) + " (" + ((nTotal - nFailFull) * 1.0 / nTotal) * 100.0  + "%)");
        log(title + "_Cold(): Pass=" + (nTotal - nFailCold) + " (" + ((nTotal - nFailCold) * 1.0 / nTotal) * 100.0  + "%)");
        log(title + "_Warm(): Pass=" + (nTotal - nFailWarm) + " (" + ((nTotal - nFailWarm) * 1.0 / nTotal) * 100.0  + "%)");
        log(title + "_Hot():  Pass=" + (nTotal - nFailHot) + " (" + ((nTotal - nFailHot) * 1.0 / nTotal) * 100.0  + "%)");

        function_log(title + "()-: " + ((nFailTotal == 0) ? "OK" : "FAILURE"));
        //toast(title + "(): " + ((nFailTotal==0)?"OK":"FAILURE"));
        return nFailTotal;
    }

    public void test09_GpsStress() {
        int nFailTotal = 0;

        nFailTotal = doStress(false, "test09_GpsStress");

        assertEquals(nFailTotal, 0);
        sTest09Result = true;
    }

/*
    public void test10_AgpsStress() {
        int nFailTotal = 0;

        nFailTotal = doStress(true, "test10_AgpsStress");

        assertEquals(nFailTotal, 0);
        sTest10Result = true;
    }
*/
}

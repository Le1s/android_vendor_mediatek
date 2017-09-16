package com.mediatek.connectivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

@SuppressWarnings(value = {"javadoctype", "javadocmethod" })

/**
  *
  * Class for epdg service for background data handoff testing.
  *
  */
public class CdsEpdgService extends Service {
    private static final String TAG = "CdsEpdgService";

    private static final int MSG_START_SERVICE = 0;

    private Context mContext;
    private Handler mHandler = new Handler();
    private static boolean sIsSrvRunning;
    private static ConnectivityManager sConnMgr = null;
    protected static final Object mLock = new Object();

    // The callback to register when we request BIP network
    private static ConnectivityManager.NetworkCallback sNetworkCallback;
    // This is really just for using the capability.
    private static NetworkRequest sNetworkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build();


    /*
     *
     * onCreate function of CdsEpdgService.
     *
     */
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mContext = this.getApplicationContext();
        sConnMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mHandler = new MyHandler(thread.getLooper());

        sIsSrvRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!sIsSrvRunning) {
            Message m = mHandler.obtainMessage(MSG_START_SERVICE);
            mHandler.sendMessage(m);
        }

        return Service.START_NOT_STICKY;
    }

    /*
     * onBind function.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    /*
     *
     * Handler class for handle aysnc tasks or events.
     *
     */
    final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_SERVICE:
                    sIsSrvRunning = true;
                    runSocketTest();
                    break;
                default:
                    break;
            }
        }

        private void runSocketTest() {
            sNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    Log.d(TAG, "onAvailable:" + network);
                    sConnMgr.setProcessDefaultNetwork(network);
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Log.d(TAG, "onLost:" + network);
                }
            };
            sConnMgr.registerNetworkCallback(sNetworkRequest, sNetworkCallback);
            Log.d(TAG, "Wait network is available");
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    Log.d(TAG, "interrupted while trying to update by index");
                }
            }
            Log.d(TAG, "Run test UDP client");
            try {
                testUdpClient();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
             } finally {
                sIsSrvRunning = false;
            }
        }

        private void testUdpClient() throws IOException {

            InetAddress testAddress =  InetAddress.getByName("8.8.8.8");
            DatagramSocket clientSocket = new DatagramSocket(12345);
            clientSocket.connect(testAddress, 12345);
            byte[] data = new byte[2];
            data[0] = 0x01;
            data[1] = 0x02;
            DatagramPacket sendSocket = new DatagramPacket(data, 2);

            while (true) {
                clientSocket.send(sendSocket);
                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "interrupted while trying to update by index");
                }
            }
        }

    }


}
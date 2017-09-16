/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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
package com.mediatek.apn.sanitytest;

import android.content.Intent;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ApnLteTests extends AndroidTestCase {

    private static final String TAG = "OmacpApnReceiverTests";
    private static final String ACTION_OMACP = "com.mediatek.omacp.settings";
    private static final String APN_SETTING_INTENT = "apn_setting_intent";
    private static final String MIME_TYPE = "application/com.mediatek.omacp-apn";

    private static final String APN_NAME = "NAP-NAME";
    private static final String APN_APN = "NAP-ADDRESS";
    private static final String APN_PROXY = "PXADDR";
    private static final String APN_PORT = "PORTNBR";
    private static final String PORT = "PORT";
    private static final String APN_TYPE = "APN-TYPE";

    private static final int SHORT_TIME = 1000;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test OMACP receive intent function.
     * */
    public void test01_sendBroadcast() {

        Intent intent = new Intent();
        intent.setAction(ACTION_OMACP);
        intent.setType(MIME_TYPE);

        ArrayList<Intent> listTests = new ArrayList<Intent>();
        // identify gemini or non-gemini to keep with the omacap itself
        for (int simId = 0; simId < TelephonyManager.getDefault().getSimCount(); simId++) {
            long[] subId = SubscriptionManager.getSubIdUsingSlotId(simId);
            // only the sim card inserted , add the intent, or will
            // exception when get its numeric from system properties
            if (subId != null && subId.length > 0) {
                Log.d(TAG, "simId = " + simId + " inserted sim card");
                listTests.add(createIntentData(simId));

                intent.putParcelableArrayListExtra(APN_SETTING_INTENT, listTests);
                mContext.sendBroadcast(intent);
                Log.d(TAG, "MTK_GEMINI_SUPPORT , SEND BROADCAST , SlotId = " + simId);
                try {
                    Thread.sleep(SHORT_TIME);
                } catch (InterruptedException e) {
                    Log.i(TAG, "InterruptedException1111");
                }
                break;
            }
        }
    }

    private Intent createIntentData(int simId) {

        Intent testIntent = new Intent();
        testIntent.putExtra("simId", simId);
        testIntent.putExtra(APN_NAME, "lab");
        testIntent.putExtra(APN_APN, "labwap3");
        testIntent.putExtra(APN_PROXY, "192.168.230.8");
        testIntent.putExtra(APN_TYPE, "");
        ArrayList<HashMap<String, String>> portList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> portMap = new HashMap<String, String>();
        portMap.put(APN_PORT, "9028");
        portList.add(portMap);
        testIntent.putExtra(PORT, portList);

        return testIntent;
    }
}

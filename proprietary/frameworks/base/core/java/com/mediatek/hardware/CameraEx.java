/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.hardware;

import android.hardware.Camera;



/**
 * The CameraEx class is used to start and stop Multi Angle View (MAV) image capture. This class
 * depends on com.android.Camera.java and the client for the Camera service,
 * which manages the actual camera hardware.
 *
 * The work-flow for your application to take a MAV photo is similar to taking a normal picture,
 * that is calling startMav instead of takePicture, and calling stopMAV after the capture number reaches
 * the number which is set when starting MAV capture.In addition, you can stop MAV during the capture process,
 * and then set the parameter[isMerge] to false to discard the capture.
 */
public class CameraEx {
    private static final String TAG = "CameraEx";
    private CameraEx() {}

    /**
     * An interface which contains a callback for Multi Angle View (MAV) image capture.
     * You must implements it and define the behavior,
     * e.g. with a progress bar to show the progress of the MAV capture.
     */
    public interface MavCallback
    {
        /**
         * Callback when capture of a MAV frame is complete.
         * For example, if the first parameter of startMav is 25 (frames to b captured),
         * onFrame() will be called 25 times, after which you should call stopMav
         * and set the parameter [isMerge] to true to save the MAV picture.
         * onFrame() will then be called a 26th time to inform you the picture is ready.
         *
         * @param jpegData The jpeg data callback of MAV image capture
         */
        void onFrame(byte[] jpegData);
    }

    /**
     * The method start the capture of a number of images into a MAV.
     * The maximum-number of images is 25 and minimum-number is 1.
     *
     * @param num Total number of images to include in the MAV
     * @param camera Instance of com.android.hardware.Camera.
     */
    public static void startMav(int num, Camera camera) {
    camera.startMav(num);
    }

    /**
     * Set MavCallback listener to a camera device.
     *
     * @param cb Listener of MavCallback
     * @param camera Instance of com.android.hardware.Camera.
     */
    public static void setMavCallback(final MavCallback cb, Camera camera) {
        android.hardware.Camera.MavCallback mMavCallback = new android.hardware.Camera.MavCallback() {
            public void onFrame(byte[] jpegData) {
                if (cb != null) {
                    cb.onFrame(jpegData);
                }
            }
        };
    camera.setMavCallback(mMavCallback);
    }

    /**
     * The method stops the MAV capture. The isMerge flag indicates if the file should be saved or discarded, 1 saves the file, 0 discards it.
     * If isMerge is set to 1, there is a callback when the files save is complete.
     *
     * @param isMerge Flag to indicate whether the captured images should be merged into a MAV file or discarded
     * @param camera Instance of com.android.hardware.Camera.
     */
    public static void stopMav(int isMerge, Camera camera) {
    camera.stopMav(isMerge);
    }
}

/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2013. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.mage.plant.p2;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.Time;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.mediatek.ngin3d.ActorNode;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Layer;
import com.mediatek.ngin3d.Light;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Quaternion;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Vec3;
import com.mediatek.ngin3d.android.StageWallpaperService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a rough "spring-based" simulation using skeletal animation, written
 * in preparation for the 3D Plant Live Wallpaper app, as a demonstration that
 * we can do the kind of thing in MAGE.
 */

public class MoneyPlantWallpaperService extends StageWallpaperService {

    public MoneyPlantWallpaperService() {
        super();
    }

    @Override
    public Engine onCreateEngine() {
        return new MoneyPlantEngine();
    }

    public class MoneyPlantEngine extends StageEngine implements
        SensorEventListener {

        public static final String TAG = "MoneyPlantWallpaperService";

        private final Stage mStage;

        private final Layer mPlantLayer = new Layer();
        private final Layer mFireflyLayer = new Layer();
        private Light mLight;
        private float mTimeOffset;

        private SensorManager mSensorManager;
        SeekBar mSeekBar;

        private Point mTouchPoint;
        private Point mLastTouch = new Point();

        Vec3 mGravityOffset = new Vec3();
        private float mTimeDown;

        // Frequency with which the physics state is updated
        private static final long UPDATE_PERIOD_MS = 10;

        private Sensor mAccelerometer;

        private Glo3D mPlant;

        private float mXRot;
        private float mYRot;
        private Quaternion mOriginalRootRotation = new Quaternion();
        private Quaternion mCameraRotation;
        private Quaternion mPullRotation = new Quaternion();

        private Joint.JointPhysics[] mPhysics = new Joint.JointPhysics[3];
        private Joint.GlobalPhysics mGlobalPhysics = new Joint.GlobalPhysics();

        private static final int LEAF_TYPE = 0;
        private static final int BRANCH_TYPE = 1;
        private static final int TRUNK_TYPE = 2;
        private static final float BETTERY_LEVEL1 = 0.5f;
        private static final float BETTERY_LEVEL2 = 0.3f;
        private static final float BETTERY_LEVEL3 = 0.1f;

        private static final float GRAVITY = 0.0002f;
        private Vec3 mSceneGravity = new Vec3(0, -GRAVITY, 0);

        private final Vec3 FLOOR_PLANE_NORMAL = new Vec3(0f, 1f, 0f);
        private static final float FLOOR_PLANE_DISTANCE = -0.34f;

        // Check the battery level every 5 minutes
        protected static final long BATTERY_CHECK_PERIOD_MS = 5 * 60 * 1000;
        private long mLastBatteryCheck;

        private final Vec3 CAMERA_POSITION = new Vec3(19.367f, 49.415f, 27.567f);
        private final Vec3 CAMERA_TARGET = new Vec3(-11.859f, -4.376f, -16.161f);
        private final float PORTAIT_FOV = 23.9f;
        private final float LANDSCAPE_FOV = 32.0f;

        private AudioManager mAudioManager;

        private MediaRecorder mMicRecorder;

        private Joint[] mNodes;
        private ActorNode[] mGrowNodes = new ActorNode[0];

        private long mElapsedLast;

        private boolean mCharging;

        Display mDisplay;

        private Time mTimeOfDay = new Time();

        ValueAnimator mScaleAnim;

        Firefly mFireflies[] = new Firefly[4];

        private int mLightOrder = -1;

        public MoneyPlantEngine() {
            super();
            mStage = getStage();
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }

        private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                mCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
                updateBatteryLevel();
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            getApplicationContext().registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            /*
             * It is good practice to listen to touch events on the StageView,
             * rather than overriding Activity.onTouchEvent(). See the Hit Test
             * Spark or MAGE Hit Test document for additional details.
             */
            //mStageView.setOnTouchListener(this);

            // Get an instance of the SensorManager
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            // Bind to the sensor
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            mDisplay = wm.getDefaultDisplay();

            Rotation cameraRot = new Rotation();
            cameraRot = Rotation.pointAt(new Vec3(0f, 0f, -1f),
                CAMERA_TARGET.subtract(CAMERA_POSITION),
                Vec3.Y_AXIS, Vec3.Y_AXIS);
            mCameraRotation = cameraRot.getQuaternion();

            mPlantLayer.setProjectionMode(Layer.PERSPECTIVE);
            mPlantLayer.setCameraNear(0.1f);
            mPlantLayer.setCameraFar(100f);
            mPlantLayer.setCameraFov(PORTAIT_FOV);
            mPlantLayer.setCameraPosition(CAMERA_POSITION);
            mPlantLayer.setCameraRotation(cameraRot);

            mFireflyLayer.setProjectionMode(Layer.PERSPECTIVE);
            mFireflyLayer.setCameraNear(0.1f);
            mFireflyLayer.setCameraFar(100f);
            mFireflyLayer.setCameraFov(PORTAIT_FOV);
            mFireflyLayer.setCameraPosition(CAMERA_POSITION);
            mFireflyLayer.setCameraRotation(cameraRot);

            Vec3 unitRot = new Vec3();
            MathUtil.setVectorToProductOf(unitRot, mCameraRotation, Vec3.X_AXIS);

            unitRot = unitRot.subtract(Vec3.dotProduct(unitRot, Vec3.Y_AXIS), unitRot);
            MathUtil.setQuaternionFromTo(mPullRotation, Vec3.X_AXIS, unitRot);

            mStage.add(mPlantLayer, mFireflyLayer);

            mPhysics[LEAF_TYPE] = new Joint.JointPhysics("Leaf", 0.1f, 0.54f, slideToDampValue(0.933f));
            mPhysics[BRANCH_TYPE] = new Joint.JointPhysics("Branch", 0.075f, 0.54f, slideToDampValue(0.642f));
            mPhysics[TRUNK_TYPE] = new Joint.JointPhysics("Trunk", 0.075f, 0.4f, slideToDampValue(0.5f));

            loadPlant("money_plant.txt");

            mLight = new Light();
            mLight.setTypePoint();
            mLight.setIsAttenuated(false);
            mLight.setColor(new Color(255, 255, 255, 255));
            mLight.setIntensity(1f);
            mLight.setPosition(new Point(15.45f, 101.0f, 0.77f));
            mPlantLayer.add(mLight);

            mFireflies[0] = new Firefly(new Point(1.f, 34.f, 2.f));
            mFireflies[1] = new Firefly(new Point(-3.f, 20.f, 1.f));
            mFireflies[2] = new Firefly(new Point(1.f, 15.f, -4.f));
            mFireflies[3] = new Firefly(new Point(-2.f, 10.f, -1.f));

            mXRot = 0f;
            updateRootRotation();

            ValueAnimator rotAnim = ValueAnimator.ofFloat(160f, 0f);
            rotAnim.setDuration(1000);
            rotAnim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator ani) {
                    Float t = (Float) ani.getAnimatedValue();
                    mGlobalPhysics.mFold.set(Vec3.Y_AXIS, t);
                }
            });
            rotAnim.start();

            mScaleAnim = ValueAnimator.ofFloat(0.01f, 1f);
            mScaleAnim.setDuration(2500);
            mScaleAnim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator ani) {
                    Float t = (Float) ani.getAnimatedValue();
                    Scale scale = new Scale(t, t, t);
                    for (ActorNode node : mGrowNodes) {
                        node.setScale(scale);
                    }
                }
            });
            mScaleAnim.start();

            ValueAnimator danceAnim = ValueAnimator.ofFloat(0f, (float) Math.PI * 2f);
            danceAnim.setDuration(750);
            danceAnim.setRepeatCount(ValueAnimator.INFINITE);
            danceAnim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator ani) {
                    if (mAudioManager.isMusicActive()) {
                        float volume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) /
                            (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        Float t = (Float) ani.getAnimatedValue();
                        Vec3 impulse = mCameraRotation.applyTo(Vec3.Y_AXIS);
                        addImpulse(impulse, (float) Math.sin(t * 2) * volume);
                        Vec3 impulse2 = mCameraRotation.applyTo(Vec3.X_AXIS);
                        addImpulse(impulse2, (float) Math.sin(t) * volume);
                    }
                }
            });
            danceAnim.start();

            animateLight();

            mElapsedLast = SystemClock.elapsedRealtime();
            mLastBatteryCheck = mElapsedLast;
            mUpdateHandler.post(mUpdateTask);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            mPlantLayer.setViewportSize(width, height);
            mFireflyLayer.setViewportSize(width, height);

            float fov = width > height ? LANDSCAPE_FOV : PORTAIT_FOV;

            mPlantLayer.setCameraFov(fov);
            mFireflyLayer.setCameraFov(fov);
        }

        private class Firefly {
            Firefly(Point p) {
                mModel = Glo3D.createFromAsset("firefly.glo");
                mModel.setMaterial("firefly.mat");
                mFireflyLayer.add(mModel);
                mLight = new Light();
                mLight.setTypePoint();
                mLight.setIsAttenuated(true);
                mLight.setAttenuationNear(4f);
                mLight.setAttenuationFar(10f);
                mLight.setColor(new Color(255, 255, 128, 255));
                mLight.setIntensity(1.0f);
                mPlantLayer.add(mLight);
                setPosition(p);
            }

            void setPosition(Point p) {
                mPoint.set(p);
                mLight.setPosition(p);
                mModel.setPosition(p);
            }

            void setPosition(float x, float y, float z) {
                mPoint.set(x, y, z);
                mLight.setPosition(mPoint);
                mModel.setPosition(mPoint);
            }

            void setBrightness(float brightness) {
                mLight.setIntensity(brightness);
                mModel.setMaterialProperty("M_OPACITY", brightness);
            }

            Glo3D mModel;
            Light mLight;
            Point mPoint = new Point();
        }

        private void updateBatteryLevel() {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
            int lvl = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = lvl / (float) scale;
            int batteryLevel;

            if (batteryPct > BETTERY_LEVEL1) {
                batteryLevel = 0;
            } else if (batteryPct > BETTERY_LEVEL2) {
                batteryLevel = 1;
            } else if (batteryPct > BETTERY_LEVEL3) {
                batteryLevel = 2;
            } else {
                batteryLevel = 3;
            }

            mPhysics[BRANCH_TYPE].mSpringStrength = 0.54f;
            mPhysics[LEAF_TYPE].mSpringStrength = 0.54f;
            mPhysics[TRUNK_TYPE].mSpringStrength = 0.4f;
            int level = mCharging ? 0 : batteryLevel;
            if (mCharging && batteryLevel == 1) {
                mPlant.setMaterialProperty("leaf073", "CHARGING", true);
                mPlant.setMaterialProperty("Cylinder021", "CHARGING", true);
            } else {
                mPlant.setMaterialProperty("leaf073", "CHARGING", false);
                mPlant.setMaterialProperty("Cylinder021", "CHARGING", false);
            }
            switch (level) {
            case 0:
                mPlant.setMaterialProperty("leaf073", "M_DIFFUSE_TEXTURE", "leaf_A_01_.png");
                break;
            case 1:
                mPlant.setMaterialProperty("leaf073", "M_DIFFUSE_TEXTURE", "leaf_A_02_.png");
                break;
            case 2:
                mPlant.setMaterialProperty("leaf073", "M_DIFFUSE_TEXTURE", "leaf_A_03_.png");
                break;
            case 3:
                mPhysics[BRANCH_TYPE].mSpringStrength = 0.1f;
                mPhysics[LEAF_TYPE].mSpringStrength = 0.1f;
                mPhysics[TRUNK_TYPE].mSpringStrength = 0.1f;
                mPlant.setMaterialProperty("leaf073", "M_DIFFUSE_TEXTURE", "leaf_A_03_.png");
                break;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            Vec3 screenGrav = null;
            int rot = mDisplay.getRotation();

            float x = event.values[0] * GRAVITY;
            float y = event.values[1] * GRAVITY;
            float z = event.values[2] * GRAVITY;

            /*
             * Sensor values are relative to the 'natural' orientation of the
             * device, so need to be rotated according to the current orientation.
             */
            switch (rot) {
            case Surface.ROTATION_0:
                screenGrav = new Vec3(x, y, z);
                break;
            case Surface.ROTATION_90:
                screenGrav = new Vec3(-y, x, z);
                break;
            case Surface.ROTATION_180:
                screenGrav = new Vec3(-x, -y, z);
                break;
            case Surface.ROTATION_270:
                screenGrav = new Vec3(y, -x, z);
                break;
            }

            mSceneGravity = mCameraRotation.applyTo(screenGrav);
            updateGravity();
        }

        private void updateGravity() {
            final float alpha = 0.5f;
            final float beta = 1 - alpha;

            Vec3 modifiedGravity = new Vec3();
            MathUtil.setVectorToSumOf(modifiedGravity, mSceneGravity, mPullRotation.applyTo(mGravityOffset));
            Vec3 newGrav = new Vec3(-modifiedGravity.x, modifiedGravity.z, -modifiedGravity.y);

            Vec3 gravity = mGlobalPhysics.mGravity;

            gravity.x = (alpha * gravity.x) + (beta * newGrav.x);
            gravity.y = (alpha * gravity.y) + (beta * newGrav.y);
            gravity.z = (alpha * gravity.z) + (beta * newGrav.z);
        }

        private float mAnimTime;

        private void animateCharging() {
            if (!mScaleAnim.isRunning()) {
                if (mCharging) {
                    mAnimTime += UPDATE_PERIOD_MS / 300f;
                    if (mAnimTime > Math.PI * 2f) {
                        mAnimTime = 0;
                    }

                    //Vec3 impulse = mCameraRotation.applyTo(Vec3.Z_AXIS);
                    //addImpulse(Vec3.Z_AXIS, (float)Math.sin(mAnimTime));

                    float t = 1f + (float) Math.sin(mAnimTime) * 0.005f;
                    Scale scale = new Scale(t, t, t);
                    scaleNodes(scale);

                    float angle = (float) Math.sin(mAnimTime) * 7f;
                    mPhysics[LEAF_TYPE].mFold.set(Vec3.Y_AXIS, angle);
                    if (mAnimTime > Math.PI * 0.6f && mAnimTime < Math.PI * 1.6f) {
                        mPlant.setMaterialProperty("leaf073", "CHARGING", true);
                        mPlant.setMaterialProperty("Cylinder021", "CHARGING", true);
                        float p = (float) ((mAnimTime - Math.PI * 0.6f) / Math.PI);
                        mPlant.setMaterialProperty("Cylinder021", "CHARGEPHASE", p);
                        mPlant.setMaterialProperty("leaf073", "CHARGEPHASE", p);
                    } else {
                        mPlant.setMaterialProperty("Cylinder021", "CHARGING", false);
                        mPlant.setMaterialProperty("leaf073", "CHARGING", false);
                    }

                    //mPlant.setMaterialProperty("Cylinder021", "CHARGEPHASE", mAnimTime / (2f * (float)Math.PI) );
                } else {
                    mPhysics[LEAF_TYPE].mFold.idt();
                    Scale scale = new Scale(1, 1, 1);
                    scaleNodes(scale);
                }
            }
        }

        private float mBlowTime;

        private void animateBlowing() {
            if (mMicRecorder != null) {
                mBlowTime += UPDATE_PERIOD_MS / 50f;
                if (mBlowTime > Math.PI * 2f) {
                    mBlowTime = 0;
                }
                float amp = mMicRecorder.getMaxAmplitude() / 2700.0f;
                Vec3 impulse = mCameraRotation.applyTo(Vec3.Y_AXIS);
                addImpulse(impulse, amp * 2f);

                Vec3 impulse2 = mCameraRotation.applyTo(Vec3.X_AXIS);
                addImpulse(impulse2, 0.2f * amp * (float) Math.sin(mBlowTime));
            }
        }

        private float mFireflyTime;
        private Rotation mLightRotation = new Rotation();
        private Vec3 mLightAxis = new Vec3(0f, 0f, 1f);
        private Vec3 mLightOrigin = new Vec3(0f, -100f, 20f);
        private Color mLightColour = new Color(255, 255, 255, 255);
        private Color mLightDay = new Color(255, 255, 255, 255);
        private Color mLightDusk = new Color(255, 128, 0, 255);

        private float saturate(float t) {
            return Math.min(1f, Math.max(t, 0f));
        }

        private float smoothstep(float edge0, float edge1, float t) {
            t = saturate((t - edge0) / (edge1 - edge0));
            return 3f * t * t - 2f * t * t * t;
        }

        private void mix(Color result, Color c1, Color c2, float t) {
            result.red = (int) ((1f - t) * c1.red + t * c2.red);
            result.green = (int) ((1f - t) * c1.green + t * c2.green);
            result.blue = (int) ((1f - t) * c1.blue + t * c2.blue);
        }

        private void animateLight() {
            mTimeOfDay = new Time();
            mTimeOfDay.setToNow();
            float timeNow = (mTimeOfDay.hour * 60 + mTimeOfDay.minute) * 60 + mTimeOfDay.second;
            timeNow = (timeNow / (24f * 60f * 60f) + mTimeOffset / 24f) % 1f;
            float brightness = (timeNow < 0.5f ? timeNow : 1f - timeNow) * 2f;

            mLightRotation.set(mLightAxis, timeNow * 360f);

            mLight.setIntensity(1.0f);
            mix(mLightColour, mLightDusk, mLightDay, smoothstep(0.5f, 1.0f, brightness));
            mLight.setColor(mLightColour);

            mLight.setPosition(mLightRotation.getQuaternion().applyTo(mLightOrigin));
            mFireflyTime += 0.01f;

            float brightnessPeriod[] = {2.3f, 2.2f, 2.53f, 2.32f};
            for (int i = 0; i < 4; ++i) {
                mFireflies[i].setBrightness(smoothstep(0.6f, 0.4f, brightness) *
                    ((float) Math.sin(mFireflyTime * brightnessPeriod[i] * 2f) * 0.5f + 0.5f));
            }
            mFireflies[0].setPosition(
                (float) Math.sin(mFireflyTime) * 4.3f,
                (float) Math.sin(mFireflyTime * 0.3) * 15f + 15f,
                (float) Math.sin(mFireflyTime * 0.9) * 3.1f
            );
            mFireflies[1].setPosition(
                (float) Math.sin(mFireflyTime * 0.8) * 5.2f,
                (float) Math.sin(mFireflyTime * 0.4) * 15f + 15f,
                (float) Math.sin(mFireflyTime * 0.9) * 3.2f
            );
            mFireflies[2].setPosition(
                (float) Math.sin(mFireflyTime * 1.3) * 3f,
                (float) Math.sin(mFireflyTime * 0.5) * 15f + 15f,
                (float) Math.sin(mFireflyTime * 2.3) * 3f
            );
            mFireflies[3].setPosition(
                (float) Math.sin(mFireflyTime * 1.1) * 5f,
                (float) Math.sin(mFireflyTime * 0.6) * 15f + 15f,
                (float) Math.sin(mFireflyTime * 0.92) * 2.5f
            );
        }

        private final Handler mUpdateHandler = new Handler();
        private final Runnable mUpdateTask = new Runnable() {
            public void run() {
                /*
                 * Make sure we don't try to simulate more than three 'ticks'
                 */
                long elapsed = SystemClock.elapsedRealtime();
                if (mElapsedLast < elapsed - UPDATE_PERIOD_MS * 3) {
                    mElapsedLast = elapsed - UPDATE_PERIOD_MS * 3;
                }
                /*
                 * Only check battery level every 5 minutes.
                 */
                if (mLastBatteryCheck < elapsed - BATTERY_CHECK_PERIOD_MS) {
                    mLastBatteryCheck = elapsed;
                    updateBatteryLevel();
                }
                while (mElapsedLast < elapsed) {

                    animateCharging();
                    animateBlowing();
                    animateLight();

                    mElapsedLast += UPDATE_PERIOD_MS;
                    int nodeCount = mNodes.length;
                    for (int i = 0; i < nodeCount; ++i) {
                        mNodes[i].updateSpringPosition();
                    }

                    for (int i = 0; i < nodeCount; ++i) {
                        mNodes[i].updateNodePosition();
                    }

                    for (int i = 0; i < nodeCount; ++i) {
                        mNodes[i].updateNodeRotation();
                    }
                }

                mUpdateHandler.postDelayed(mUpdateTask, UPDATE_PERIOD_MS);
            }
        };

        @Override
        public void onResume() {
            super.onResume();

            mSensorManager.registerListener(
                this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            mUpdateHandler.post(mUpdateTask);
            mElapsedLast = SystemClock.elapsedRealtime();

            if (mMicRecorder == null) {
                mMicRecorder = new MediaRecorder();
                mMicRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMicRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mMicRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mMicRecorder.setOutputFile("/dev/null");
                try {
                    mMicRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMicRecorder.start();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            mSensorManager.unregisterListener(this);
            mUpdateHandler.removeCallbacks(mUpdateTask);

            if (mMicRecorder != null) {
                mMicRecorder.stop();
                mMicRecorder.release();
                mMicRecorder = null;
            }
        }

        private void addImpulse(Vec3 impulse, float scale) {
            for (int i = 0; i < mNodes.length; ++i) {
                mNodes[i].mVelocity.x += impulse.x * .01f * scale;
                mNodes[i].mVelocity.y += impulse.y * .01f * scale;
                mNodes[i].mVelocity.z += impulse.z * .01f * scale;
            }
        }

        private void scaleNodes(Scale scale) {
            for (int i = 0; i < mNodes.length; ++i) {
                if (mNodes[i].mPhysics == mPhysics[TRUNK_TYPE]) {
                    mNodes[i].mActorNode.setScale(scale);
                }
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                mTouchPoint = new Point(event.getX(), event.getY());
                mLastTouch.set(mTouchPoint);
                mTimeDown = event.getEventTime();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                //if(mTouchPoint.y <= mSpinZone) {
                if ((event.getEventTime() - mTimeDown) < 500) {
                    //launchEmail();
                    Vec3 impulse = mCameraRotation.applyTo(Vec3.Y_AXIS);
                    addImpulse(impulse, 1f);
                }
                //}
                mGravityOffset.set(0f, 0f, 0f);
                mGlobalPhysics.mExtraDamping = 1f;
                updateGravity();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float xChange = (event.getX() - mTouchPoint.x) * 0.000005f;
                float yChange = (event.getY() - mTouchPoint.y) * 0.000005f;
                mGravityOffset.set(-xChange, 2.5f * (float) Math.sqrt(xChange * xChange + yChange * yChange), -yChange);
                mGlobalPhysics.mExtraDamping = 0.995f;
                updateGravity();
            }

            super.onTouchEvent(event);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xOffsetStep, float yOffsetStep,
                                     int xPixels, int yPixels) {
            /*
             * This rotates the plant when the launcher window is dragged
             * left-right
             */
            mXRot = xPixels * 0.5f;
            updateRootRotation();
        }

        private void loadPlant(String name) {

            ArrayList<Joint> joints = new ArrayList<Joint>();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                    getApplicationContext().getAssets().open(name)));
                String line;

                // Read glo file name, (optionally) scale and (optionally) floor
                if ((line = br.readLine()) != null) {
                    String[] ar = line.split(",");
                    mPlant = Glo3D.createFromAsset(ar[0]);
                    mPlantLayer.add(mPlant);
                    if (ar.length > 1 && !ar[1].isEmpty()) {
                        float s = Float.parseFloat(ar[1]);
                        mPlant.setScale(new Scale(s, s, s));
                    }
                    if (ar.length > 2 && !ar[2].isEmpty()) {
                        Glo3D floor = Glo3D.createFromAsset(ar[2]);
                        mPlantLayer.add(floor);
                    }
                    mPlant.setMaterialProperty("Plane001", "M_SELF_ILLUMINATION", 0f);
                }

                // Read names of shadow nodes and set to shadow material
                if ((line = br.readLine()) != null) {
                    String[] shadow_nodes = line.split(",");
                    for (String node : shadow_nodes) {
                        if (!node.isEmpty()) {
                            mPlant.setMaterial(node, "shadow.mat");
                            mPlant.setMaterialProperty(node, "T_SHADOW_PLANE_NORMAL",
                                FLOOR_PLANE_NORMAL);
                            mPlant.setMaterialProperty(node, "T_SHADOW_PLANE_DISTANCE",
                                FLOOR_PLANE_DISTANCE);
                        }
                    }
                }
                mPlant.setMaterial("leaf073", "leaf.mat");
                mPlant.setMaterial("Cylinder021", "leaf.mat");
                mPlant.setMaterialProperty("leaf073", "M_EMISSIVE_TEXTURE", "perlin_noise.png");
                mPlant.setMaterialProperty("leaf073", "M_SELF_ILLUMINATION", 0.4f);

                if ((line = br.readLine()) != null) {
                    if (!line.isEmpty()) {
                        String[] ar = line.split(",");
                        mGrowNodes = new ActorNode[ar.length];
                        for (int i = 0; i < ar.length; ++i) {
                            mGrowNodes[i] = mPlant.getNode(ar[i]);
                        }
                    }
                }

                int joint = 0;
                HashMap<String, Joint> map = new HashMap<String, Joint>();
                while ((line = br.readLine()) != null) {
                    String[] ar = line.split(",");
                    float x = Float.parseFloat(ar[2]);
                    float y = Float.parseFloat(ar[3]);
                    float z = Float.parseFloat(ar[4]);
                    float a = Float.parseFloat(ar[5]);
                    float b = Float.parseFloat(ar[6]);
                    float c = Float.parseFloat(ar[7]);
                    float d = Float.parseFloat(ar[8]);
                    int type = TRUNK_TYPE;
                    if (ar[0].contains("leaf")) {
                        type = LEAF_TYPE;
                    } else if (ar[0].contains("branch")) {
                        type = BRANCH_TYPE;
                    }
                    Joint j = new Joint(mPlant.getNode(ar[0]), map.get(ar[1]),
                        x, y, z, a, b, c, d, mPhysics[type], mGlobalPhysics);
                    joints.add(j);
                    //mNodes[joint] = j;
                    map.put(ar[0], j);

                    if (joint == 0) {
                        mOriginalRootRotation = new Quaternion(a, b, c, d);
                    }

                    ++joint;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            mNodes = new Joint[joints.size()];
            joints.toArray(mNodes);
        }

        private void updateRootRotation() {
            Quaternion r = new Quaternion();
            r.set(mOriginalRootRotation);
            r.multiply(new Quaternion(Vec3.Z_AXIS, mXRot));
            r.multiply(new Quaternion(Vec3.X_AXIS, mYRot));
            mNodes[0].setRotation(r);
        }

        private float slideToDampValue(float slide) {
            return (float) (1 - Math.pow(10, (slide - 2)));
        }
    }
}

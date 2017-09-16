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
 * MediaTek Inc. (C) 2010. All rights reserved.
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

#ifndef AUDIO_SPEAKER_MONITOR_CUSTOM_H
#define AUDIO_SPEAKER_MONITOR_CUSTOM_H

/****************************************************
* Define default Speaker Monitor Param
*****************************************************/

#define DEFAULT_SPEAKER_MONITOR_PARAM \
0.000000f,   \
0.000000f,   \
0.000000f,   \
8.380327f,   \
8.618719f,   \
8.458954f,   \
8.554009f,   \
8.419544f,   \
8.516932f,   \
8.497550f,   \
8.434443f,   \
8.531321f,   \
8.529181f,   \
8.455778f,   \
8.556326f,   \
8.590837f,   \
0.000000f,   \
8.518906f,   \
8.578416f,   \
8.436659f,   \
8.471119f,   \
8.758801f,   \
8.736255f,   \
0.000000f,   \
0.000000f,   \
8.664410f,   \
8.823398f,   \
8.893615f,   \
8.801714f,   \
8.899089f,   \
8.952518f,   \
8.949934f,   \
9.041879f,   \
9.125337f,   \
9.159996f,   \
9.358884f,   \
9.245324f,   \
9.134375f,   \
9.120094f,   \
9.061420f,   \
9.000664f,   \
8.958218f,   \
8.946178f,   \
8.803001f,   \
8.757942f,   \
8.734652f,   \
8.715337f,   \
8.634418f,   \
8.630900f,   \
8.610519f,   \
8.564054f,   \
8.564524f,   \
8.523356f,   \
8.611408f,   \
8.665421f,   \
8.754838f,   \
8.607685f,   \
8.735320f,   \
8.641224f,   \
8.421438f,   \
8.658829f,   \
8.812174f,   \
8.725631f,   \
8.521318f,   \
8.764711f,   \
8.559218f,   \
8.542862f,   \
8.589477f,   \
8.813379f,   \
8.729831f,   \
8.760648f,   \
8.767310f,   \
8.790205f,   \
8.646417f,   \
8.799109f,   \
8.709496f,   \
8.788063f,   \
8.768193f,   \
8.607593f,   \
8.703308f,   \
8.697216f,   \
8.641158f,   \
8.490874f,   \
8.561043f,   \
8.564624f,   \
8.642116f,   \
8.529763f,   \
8.607647f,   \
8.645239f,   \
8.489223f,   \
8.566134f,   \
8.660070f,   \
8.697948f,   \
8.606365f,   \
8.625107f,   \
8.462513f,   \
8.506654f,   \
8.609536f,   \
8.587020f,   \
8.428434f,   \
8.571095f,   \
8.568141f,   \
8.680489f,   \
8.689684f,   \
8.580139f,   \
8.503972f,   \
8.525538f,   \
8.434114f,   \
8.428564f,   \
8.623198f,   \
8.318935f,   \
8.697640f,   \
8.640573f,   \
8.645565f,   \
8.530322f,   \
8.541785f,   \
8.587063f,   \
8.523448f,   \
8.850811f,   \
8.626362f,   \
8.591326f,   \
8.579216f,   \
8.722513f,   \
8.694804f,   \
8.600140f,   \
8.576862f,   \
8.570396f,   \
8.557280f,   \
8.622511f,   \
8.542190f,   \
8.654620f,   \
8.607939f,   \
8.530720f,   \
8.574299f,   \
8.601932f,   \
8.579507f,   \
8.814807f,   \
8.665043f,   \
8.712315f,   \
8.901777f,   \
8.739564f,   \
8.680674f,   \
8.707913f,   \
8.578209f,   \
8.727027f,   \
8.641418f,   \
8.741638f,   \
8.640203f,   \
8.774293f,   \
8.658672f,   \
8.661994f,   \
8.738991f,   \
8.782119f,   \
8.655685f,   \
8.809863f,   \
8.780890f,   \
8.833199f,   \
8.784634f,   \
8.731490f,   \
8.823947f,   \
8.978390f,   \
9.022103f,   \
8.970873f,   \
8.904754f,   \
8.964508f,   \
8.760936f,   \
8.752891f,   \
8.842197f,   \
8.740192f,   \
8.846971f,   \
8.731956f,   \
8.806735f,   \
8.768459f,   \
8.715538f,   \
8.794477f,   \
8.914141f,   \
8.856139f,   \
8.731854f,   \
8.785730f,   \
8.900426f,   \
8.802038f,   \
8.824956f,   \
8.894501f,   \
8.936224f,   \
8.924375f,   \
8.966758f,   \
9.049627f,   \
8.893126f,   \
8.887155f,   \
8.805160f,   \
8.800812f,   \
8.989094f,   \
8.804363f,   \
9.025553f,   \
9.132754f,   \
0.000000f,   \
8.919987f,   \
8.931311f,   \
8.914665f,   \
8.890076f,   \
8.771264f,   \
8.848402f,   \
8.799211f,   \
8.895363f,   \
8.795221f,   \
8.841002f,   \
8.923860f,   \
8.968747f,   \
8.898819f,   \
8.875841f,   \
8.979981f,   \
8.883396f,   \
8.944675f,   \
8.984742f,   \
8.983413f,   \
8.941082f,   \
8.811439f,   \
8.919549f,   \
8.915738f,   \
8.918367f,   \
8.949731f,   \
9.017003f,   \
8.841352f,   \
8.876965f,   \
9.019051f,   \
8.907249f,   \
8.849182f,   \
8.840283f,   \
8.915133f,   \
9.006112f,   \
8.834620f,   \
9.039268f,   \
8.864918f,   \
8.931136f,   \
9.189347f,   \
8.813417f,   \
8.857396f,   \
8.833146f,   \
9.062584f,   \
9.058092f,   \
8.933196f,   \
8.942034f,   \
9.175007f,   \
9.078342f,   \
9.040699f,   \
9.019749f,   \
8.907639f,   \
9.088775f,   \
9.000766f,   \
9.057095f,   \
9.014068f,   \
9.060398f,   \
8.996405f,   \
9.073402f,   \
9.101326f,   \
9.048927f,   \
8.993359f,   \
9.088068f,   \
9.019467f,   \
8.920594f,   \
9.089280f,   \
9.090373f,   \
9.063709f,   \
9.027975f,   \
8.935629f,   \
9.197655f,   \
9.102942f,   \
9.186192f,   \
9.050220f,   \
0.000000f,   \
8.933355f,   \
9.240149f,   \
9.278635f,   \
9.363556f,   \
9.186421f,   \
9.198982f,   \
9.096582f,   \
8.790647f,   \
9.133729f,   \
0.000000f,   \
9.254603f,   \
9.131580f,   \
9.208569f,   \
9.068190f,   \
9.069460f,   \
9.104443f,   \
0.000000f,   \
8.864295f,   \
9.149402f,   \
9.134187f,   \
9.084586f,   \
9.132801f,   \
9.283607f,   \
9.192044f,   \
9.174462f,   \
9.163030f,   \
9.069324f,   \
9.159952f,   \
9.084007f,   \
9.145996f,   \
8.993873f,   \
9.087499f,   \
9.045495f,   \
9.178195f,   \
9.135751f,   \
9.092221f,   \
8.870635f,   \
9.088806f,   \
9.118202f,   \
9.217866f,   \
9.196878f,   \
9.192537f,   \
9.143693f,   \
8.902993f,   \
0.000000f,   \
0.000000f,   \
9.330952f,   \
9.161581f,   \
9.183514f,   \
9.298921f,   \
0.000000f,   \
9.205456f,   \
9.271367f,   \
9.173865f,   \
9.437585f,   \
9.242805f,   \
8.978479f,   \
9.286263f,   \
9.337500f,   \
9.239666f,   \
9.386067f,   \
9.303203f,   \
9.136268f,   \
9.025937f,   \
9.025501f,   \
9.334415f,   \
9.192081f,   \
9.151661f,   \
9.180889f,   \
9.216934f,   \
9.207030f,   \
9.146996f,   \
9.252947f,   \
9.242410f,   \
9.142612f,   \
9.309297f,   \
9.190456f,   \
9.290041f,   \
9.136477f,   \
9.183789f,   \
9.163765f,   \
9.144120f,   \
9.250023f,   \
0.000000f,   \
9.244972f,   \
9.230491f,   \
9.238170f,   \
9.194987f,   \
9.301810f,   \
9.244583f,   \
9.013835f,   \
0.000000f,   \
9.256285f,   \
9.058818f,   \
9.136072f,   \
9.187997f,   \
9.228507f,   \
9.454903f,   \
9.340119f,   \
9.470283f,   \
9.208138f,   \
9.409415f,   \
9.351315f,   \
9.241434f,   \
9.200359f,   \
9.605282f,   \
9.321250f,   \
9.360670f,   \
9.341038f,   \
9.282929f,   \
9.340724f,   \
9.335156f,   \
9.266579f,   \
9.213867f,   \
9.207273f,   \
9.289083f,   \
9.338340f,   \
9.358887f,   \
9.320164f,   \
9.285013f,   \
9.340792f,   \
9.348487f,   \
9.299854f,   \
9.264228f,   \
9.225918f,   \
9.283894f,   \
9.281675f,   \
9.344671f,   \
9.369980f,   \
9.426292f,   \
9.376351f,   \
9.386971f,   \
9.430260f,   \
9.344142f,   \
9.415732f,   \
9.122206f,   \
9.411487f,   \
9.246588f,   \
9.294423f,   \
0.000000f,   \
9.200039f,   \
9.690338f,   \
9.330211f,   \
9.416810f,   \
9.225365f,   \
9.524183f,   \
9.351426f,   \
9.324402f,   \
9.205568f,   \
9.315056f,   \
9.440185f,   \
9.339503f,   \
9.431277f,   \
9.507989f,   \
9.510588f,   \
9.382082f,   \
9.353218f,   \
9.427076f,   \
9.448383f,   \
9.416532f,   \
9.459359f,   \
9.480550f,   \
9.343621f,   \
9.513320f,   \
9.457984f,   \
9.416179f,   \
9.498687f,   \
9.347201f,   \
9.284859f,   \
9.209662f,   \
9.505651f,   \
9.244514f,   \
9.415538f,   \
9.478091f,   \
9.684154f,   \
9.370621f,   \
9.382353f,   \
0.000000f,   \
9.495836f,   \
9.408027f,   \
9.473525f,   \
9.416968f,   \
0.000000f,   \
9.526569f,   \
9.363935f,   \
9.410586f,   \
9.485623f,   \
0.000000f,   \
9.424579f,   \
9.409324f,   \
9.558723f,   \
9.466044f,   \
0.000000f,   \
9.440201f,   \
9.458752f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
9.434142f,   \
9.515571f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f,   \
0.000000f


#define DEFAULT_SPEAKER_TEMP_INITIAL (25.0f)
#define DEFAULT_SPEAKER_CURRENT_SENSE_RESISTOR (0.4f)
#define DEFAULT_SPEAKER_RESONANT_FC (1800)
#define DEFAULT_SPEAKER_RESONANT_BW (200)
#define DEFAULT_SPEAKER_RESONANT_TH (0xFFF2)
#define DEFAULT_SPEAKER_PREFER_HIGH_BAND 90
#define DEFAULT_SPEAKER_PREFER_LOW_BAND  51
#define DEFAULT_SPEAKER_TEMP_CONTROL_HIGH 100
#define DEFAULT_SPEAKER_TEMP_CONTROL_LOW  95
#define DEFAULT_SPEAKER_TEMP_CONTROL_LOG  10
#define DEFAULT_SPEAKER_MONITOR_INTERVAL  1000
#endif

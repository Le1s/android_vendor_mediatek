/*
 * Copyright (C) 2012 The Android Open Source Project
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

#define LOG_TAG "PQ_JNI"

#include <jni_pq.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <errno.h>
#include <utils/Log.h>
#include <utils/threads.h>
#include <cutils/xlog.h>
#include <cutils/properties.h>

#ifdef MTK_MIRAVISION_IMAGE_DC_SUPPORT
#include <PQDCHistogram.h>
#endif

using namespace android;

#define UNUSED(expr) do { (void)(expr); } while (0)
#define PQ_JNI_CLASS_NAME "com/mediatek/pq/PictureQuality"

int drvID = -1;
int ret = 0;
DISP_PQ_PARAM pqparam_original;
DISP_PQ_PARAM pqparam_camera;
Mutex mLock;


static void setCameraPreviewMode(JNIEnv* env, jobject thiz)
{
    Mutex::Autolock autoLock(mLock);
    int drvID = -1;

    drvID = open("/proc/mtk_mira", O_RDONLY, 0);
    if (drvID < 0)
    {
        //XLOGE("[PQ JNI] setCameraPreviewMode, open /proc/mtk_mira failed!! ");
        drvID = open("/proc/mtk_mdp_color", O_RDONLY, 0);
    }
    if (drvID < 0)
    {
        //XLOGE("[PQ JNI] setCameraPreviewMode, open /proc/mtk_mdp_color failed!! ");
        drvID = open("/proc/mtk_mdp_cmdq", O_RDONLY, 0);
    }
    if (drvID < 0)
    {
        //XLOGE("[PQ JNI] setCameraPreviewMode, open /proc/mtk_mdp_cmdq failed!! ");
        drvID = open("/dev/mtk_disp", O_RDONLY, 0);
    }
    if(drvID < 0)
    {
        drvID = open("/proc/mtk_disp", O_RDONLY, 0);
    }

    ioctl(drvID, DISP_IOCTL_GET_PQ_CAM_PARAM, &pqparam_camera);

    ioctl(drvID, DISP_IOCTL_SET_PQPARAM, &pqparam_camera);

    if (drvID >= 0)
    {
        close(drvID);
    }

    UNUSED(env);
    UNUSED(thiz);

    return;
}

static void setGalleryNormalMode(JNIEnv* env, jobject thiz)
{
    Mutex::Autolock autoLock(mLock);

    int drvID = -1;

    drvID = open("/proc/mtk_mira", O_RDONLY, 0);
    if (drvID < 0)
    {
        //XLOGE("[PQ JNI] setCameraPreviewMode, open /proc/mtk_mira failed!! ");
        drvID = open("/proc/mtk_mdp_color", O_RDONLY, 0);
    }
    if (drvID < 0)
    {
        //XLOGE("[PQ JNI] setCameraPreviewMode, open /proc/mtk_mdp_color failed!! ");
        drvID = open("/proc/mtk_mdp_cmdq", O_RDONLY, 0);
    }
    if (drvID < 0)
    {
        //XLOGE("[PQ JNI] setCameraPreviewMode, open /proc/mtk_mdp_cmdq failed!! ");
        drvID = open("/dev/mtk_disp", O_RDONLY, 0);
    }
    if(drvID < 0)
    {
        drvID = open("/proc/mtk_disp", O_RDONLY, 0);
    }

    ioctl(drvID, DISP_IOCTL_GET_PQ_GAL_PARAM, &pqparam_original);

    ioctl(drvID, DISP_IOCTL_SET_PQPARAM, &pqparam_original);

    if (drvID >= 0)
    {
        close(drvID);
    }

    UNUSED(env);
    UNUSED(thiz);

    return;
}

static void Hist_set(JNIEnv* env, jobject obj, jint index, jint value)
{
    jclass clazz = env->FindClass(PQ_JNI_CLASS_NAME "$Hist");
    jmethodID setMethod = env->GetMethodID(clazz, "set", "(II)V");
    env->CallVoidMethod(obj, setMethod, index, value);

    env->DeleteLocalRef(clazz);
}

static void getDynamicContrastHistogram(JNIEnv* env, jclass clz, jbyteArray srcBuffer, jint srcWidth, jint srcHeight, jobject hist)
{
    Mutex::Autolock autoLock(mLock);

#ifdef MTK_MIRAVISION_IMAGE_DC_SUPPORT
    CPQDCHistogram *pDCHist = new CPQDCHistogram;
    DynCInput   input;
    DynCOutput  output;
    int i;

    input.pSrcFB = (unsigned char*)env->GetByteArrayElements(srcBuffer, 0);
    input.iWidth = srcWidth;
    input.iHeight = srcHeight;

    //XLOGD("[PQ_JNI] DCHist in, w[%d], h[%d]", srcWidth, srcHeight);
    pDCHist->Main(input, &output);

    //XLOGD("[PQ_JNI] DCHist out, hist0[%d], hist1[%d], hist2[%d], hist3[%d], hist4[%d]", output.Info[0], output.Info[1], output.Info[2], output.Info[3], output.Info[4]);
    //XLOGD("[PQ_JNI] DCHist out, hist5[%d], hist6[%d], hist7[%d], hist8[%d], hist9[%d]", output.Info[5], output.Info[6], output.Info[7], output.Info[8], output.Info[9]);
    //XLOGD("[PQ_JNI] DCHist out, hist10[%d], hist11[%d], hist12[%d], hist13[%d], hist14[%d]", output.Info[10], output.Info[11], output.Info[12], output.Info[13], output.Info[14]);
    //XLOGD("[PQ_JNI] DCHist out, hist15[%d], hist16[%d], hist17[%d], hist18[%d], hist19[%d]", output.Info[15], output.Info[16], output.Info[17], output.Info[18], output.Info[19]);

    for (i = 0; i < DCHIST_INFO_NUM; i++)
    {
        Hist_set(env, hist, i, output.Info[i]);
    }

    env->ReleaseByteArrayElements(srcBuffer, (jbyte*)input.pSrcFB, 0);
    delete pDCHist;
#else
    XLOGE("[PQ_JNI] getDynamicContrastHistogram(), not supported!");

    UNUSED(env);
    UNUSED(srcBuffer);
    UNUSED(srcWidth);
    UNUSED(srcHeight);
    UNUSED(hist);
#endif
    UNUSED(clz);
}



/////////////////////////////////////////////////////////////////////////////////

//JNI register
////////////////////////////////////////////////////////////////
static const char *classPathName = PQ_JNI_CLASS_NAME;

static JNINativeMethod g_methods[] = {
  {"nativeSetCameraPreviewMode", "()V", (void*)setCameraPreviewMode},
  {"nativeSetGalleryNormalMode", "()V", (void*)setGalleryNormalMode},
  {"nativeGetDynamicContrastHistogram", "([BIIL" PQ_JNI_CLASS_NAME "$Hist;)V", (void*)getDynamicContrastHistogram},
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        XLOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        XLOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

// ----------------------------------------------------------------------------

/*
 * This is called by the VM when the shared library is first loaded.
 */

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    UNUSED(reserved);

    XLOGI("JNI_OnLoad");

    if (JNI_OK != vm->GetEnv((void **)&env, JNI_VERSION_1_4)) {
        XLOGE("ERROR: GetEnv failed");
        goto bail;
    }

    if (!registerNativeMethods(env, classPathName, g_methods, sizeof(g_methods) / sizeof(g_methods[0]))) {
        XLOGE("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

bail:
    return result;
}

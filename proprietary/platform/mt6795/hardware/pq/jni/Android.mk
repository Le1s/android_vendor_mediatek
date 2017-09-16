LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	mhal_jni.cpp

LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)

LOCAL_C_INCLUDES += \
        $(MTK_PATH_SOURCE)/hardware/dpframework/inc \
        $(TOP)/bionic \
        $(MTK_PATH_SOURCE)/kernel/include \
        $(MTK_PATH_PLATFORM)/kernel/drivers/dispsys \
        $(MTK_PATH_PLATFORM)/hardware/pq \
        $(MTK_PATH_PLATFORM)/hardware/pq/inc \


LOCAL_SHARED_LIBRARIES := \
	libutils \
	libcutils

LOCAL_MODULE := libPQjni
LOCAL_MULTILIB := both

include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))
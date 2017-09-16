LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	pqdc_tuning_jni.cpp
	 
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)

LOCAL_C_INCLUDES += \
        $(MTK_PATH_SOURCE)/hardware/dpframework/inc \
        $(TOP)/bionic \
        $(TOP)/frameworks/base/include \
        $(MTK_PATH_PLATFORM)/kernel/drivers/dispsys \
        $(MTK_PATH_PLATFORM)/hardware/pq \
        $(MTK_PATH_PLATFORM)/hardware/pq/inc \

	
LOCAL_SHARED_LIBRARIES := \
    libutils \
    libcutils

LOCAL_MODULE := libPQDCjni
LOCAL_MULTILIB := both

include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))

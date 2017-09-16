LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	miravision_jni.cpp
	 
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)

LOCAL_C_INCLUDES += \
        $(MTK_PATH_SOURCE)/hardware/dpframework/inc \
        $(TOP)/bionic \
        $(MTK_PATH_SOURCE)/kernel/include \
        $(MTK_PATH_PLATFORM)/kernel/drivers/dispsys \
        $(MTK_PATH_PLATFORM)/hardware/pq \
        $(MTK_PATH_PLATFORM)/hardware/pq/inc \
        $(MTK_PATH_PLATFORM)/hardware/aal/inc \

	
LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils \
	libaal \
	libpq_cust

ifeq ($(strip $(MTK_AAL_SUPPORT)),yes)
    LOCAL_CFLAGS += -DMTK_AAL_SUPPORT
endif

ifeq ($(strip $(MTK_OD_SUPPORT)),yes)
    LOCAL_CFLAGS += -DMTK_OD_SUPPORT
endif

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE := libMiraVision_jni
LOCAL_MULTILIB := both

include $(BUILD_SHARED_LIBRARY)

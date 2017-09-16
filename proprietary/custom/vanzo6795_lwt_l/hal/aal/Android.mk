LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
    cust_aal.cpp

LOCAL_SHARED_LIBRARIES := \
    libcutils \

LC_MTK_PLATFORM := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )

LOCAL_C_INCLUDES += \
    $(TOP)/$(MTK_PATH_SOURCE)/platform/$(LC_MTK_PLATFORM)/hardware/aal/inc \


LOCAL_MODULE:= libaal_config

include $(BUILD_STATIC_LIBRARY)

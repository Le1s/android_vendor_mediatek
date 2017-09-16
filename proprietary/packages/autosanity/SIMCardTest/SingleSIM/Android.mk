ifeq ($(strip $(MTK_AUTO_TEST)), yes)

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# Module name should match binary name to be installed
LOCAL_MODULE := Sanity_TC_01_singleSIM.py

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_PATH := $(TARGET_OUT_APPS)
include $(BUILD_PREBUILT)

endif

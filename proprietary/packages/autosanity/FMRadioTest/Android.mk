ifeq ($(MTK_FM_SUPPORT),yes)
ifeq ($(MTK_FM_RX_SUPPORT),yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := tests

LOCAL_CERTIFICATE := platform

LOCAL_JAVA_LIBRARIES := android.test.runner
LOCAL_STATIC_JAVA_LIBRARIES := librobotium4

# Include all test java files.
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := FMRadioTest

LOCAL_INSTRUMENTATION_FOR := FmRadio

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)
endif
endif

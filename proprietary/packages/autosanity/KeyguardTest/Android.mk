
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := tests

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := android.test.runner

LOCAL_STATIC_JAVA_LIBRARIES := librobotium4

LOCAL_PACKAGE_NAME := KeyguardSanity

# Remove these to verify permission checks are working correctly
LOCAL_CERTIFICATE := platform

#LOCAL_PRIVILEGED_MODULE := true

# LOCAL_PROGUARD_FLAG_FILES := proguard.flags

#LOCAL_INSTRUMENTATION_FOR := Keyguard

include $(BUILD_PACKAGE)


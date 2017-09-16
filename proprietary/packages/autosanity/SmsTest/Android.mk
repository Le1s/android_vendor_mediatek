

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := tests
#LOCAL_CERTIFICATE := platform
# We only want this apk build for tests.
#LOCAL_MODULE_TAGS := ReceiveAndReplyMMS_GEMINI

LOCAL_JAVA_LIBRARIES := android.test.runner
LOCAL_STATIC_JAVA_LIBRARIES := librobotium4

# Include all test java files.
LOCAL_SRC_FILES := $(call all-java-files-under, src)


LOCAL_PACKAGE_NAME := SmsSanityTest

LOCAL_INSTRUMENTATION_FOR := Mms

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE := sanity_configure.xml
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_APPS)
include $(BUILD_PREBUILT)


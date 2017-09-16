#ifeq ($(strip $(MTK_AUTO_TEST)),yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := tests
LOCAL_CERTIFICATE := shared

LOCAL_JAVA_LIBRARIES := android.test.runner \
                        mediatek-framework \
                        telephony-common
LOCAL_STATIC_JAVA_LIBRARIES := librobotium4

# Include all test java files.
LOCAL_SRC_FILES := $(call all-java-files-under, src)
#LOCAL_SRC_FILES := $(call all-java-files-under, ../../ContactsCommon/src)
LOCAL_PACKAGE_NAME := ContactsSanityTest
LOCAL_INSTRUMENTATION_FOR := Contacts
include $(BUILD_PACKAGE)

#endif

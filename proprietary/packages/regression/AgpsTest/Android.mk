#ifeq ($(strip $(MTK_AUTO_TEST)), yes)
ifeq ($(MTK_AGPS_APP),yes)

    LOCAL_PATH:= $(call my-dir)
    include $(CLEAR_VARS)

    # We only want this apk build for tests.
    LOCAL_MODULE_TAGS := tests
    LOCAL_CERTIFICATE := platform

    LOCAL_JAVA_LIBRARIES := android.test.runner
    LOCAL_STATIC_JAVA_LIBRARIES := librobotium4
    LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.lbs.em2.utils

    # Include all test java files.
    LOCAL_SRC_FILES := $(call all-java-files-under, src)

    LOCAL_PACKAGE_NAME := AgpsTest

    include $(BUILD_PACKAGE)
endif
#endif

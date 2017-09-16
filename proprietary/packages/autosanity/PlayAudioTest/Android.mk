#ifeq ($(strip $(MTK_AUTO_TEST)), yes)
	LOCAL_PATH:= $(call my-dir)
	include $(CLEAR_VARS)
	
	# We only want this apk build for tests.
	
	LOCAL_MODULE_TAGS := tests
	LOCAL_CERTIFICATE := platform
	LOCAL_JAVA_LIBRARIES := android.test.runner
	
	LOCAL_STATIC_JAVA_LIBRARIES:=autoTest_utils  librobotium4
	
	# Include all test java files.
	LOCAL_SRC_FILES := $(call all-java-files-under, src)
	
	LOCAL_PACKAGE_NAME := audiotest
	
	LOCAL_INSTRUMENTATION_FOR := Music
	
	include $(BUILD_PACKAGE)
	include $(CLEAR_VARS)
	LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES:=autoTest_utils:autoTest_utils.jar	
	include $(BUILD_MULTI_PREBUILT)
#endif

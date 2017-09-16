#
$(info MAGE Money Plant android.mk)
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

#LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := Plant3D

LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.ngin3d-static

LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_SOURCE := javaclassfile
LOCAL_PROGUARD_FLAG_FILES := proguard.flags


LOCAL_JNI_SHARED_LIBRARIES := libja3m liba3m

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res

#add for JPE begin
LOCAL_JAVASSIST_ENABLED := true
LOCAL_JAVASSIST_OPTIONS := $(LOCAL_PATH)/jpe.config
#end for JPE

include $(BUILD_PACKAGE)

LOCAL_PATH:= $(call my-dir)

ifneq ($(strip $(MTK_PLATFORM)),)
ifeq ($(strip $(MTK_SECURITY_SW_SUPPORT)), yes)

###############################################################################
# SEC SBCHK APPLICATION
###############################################################################
include $(CLEAR_VARS)
LOCAL_MODULE := sbchk
LOCAL_SRC_FILES := sbchk.c
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_LIBRARIES := libsbchk
ifeq ($(CUSTOM_SEC_AUTH_SUPPORT),yes)
$(call config-custom-folder,custom:security/sbchk)
LOCAL_SRC_FILES += custom/cust_auth.c
else
LOCAL_SRC_FILES += auth/sec_wrapper.c
LOCAL_STATIC_LIBRARIES += libauth
endif
include $(BUILD_EXECUTABLE)

###############################################################################
# SEC FILE LIST
###############################################################################
# for S_ANDRO_SFL.ini
include $(CLEAR_VARS)
LOCAL_MODULE := S_ANDRO_SFL.ini
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/firmware
include $(BUILD_PREBUILT)

# for S_SECRO_SFL.ini
include $(CLEAR_VARS)
LOCAL_MODULE := S_SECRO_SFL.ini
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/secro
include $(BUILD_PREBUILT)

# for AC_REGION
LOCAL_PATH := $(MTK_PATH_CUSTOM)/secro
include $(CLEAR_VARS)
LOCAL_MODULE := AC_REGION
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_SRC_FILES := $(LOCAL_MODULE)
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/secro
include $(BUILD_PREBUILT)

endif #MTK_SECURITY_SW_SUPPORT
endif #MTK_PLATFORM


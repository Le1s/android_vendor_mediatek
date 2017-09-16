ifneq ($(strip $(MTK_EMULATOR_SUPPORT)),yes)
ifneq ($(BUILD_MTK_LDVT),yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

COMMON_PATH := $(LOCAL_PATH)/common

PLATFORM_PATH := $(MTK_PATH_PLATFORM)/external/meta
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
endif


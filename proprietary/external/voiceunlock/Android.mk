ifneq ($(strip $(MTK_VOICE_UNLOCK_SUPPORT))_$(strip $(MTK_VOW_SUPPORT)), no_no)
LOCAL_PATH := $(my-dir)

include $(CLEAR_VARS)

ifneq ($(strip $(MTK_VOICE_UNLOCK_USE_TAB_LIB)),yes)
LOCAL_MODULE := libvoiceunlock
LOCAL_SRC_FILES_arm := libvoiceunlock_32.a
LOCAL_SRC_FILES_arm64 := libvoiceunlock_32.a
#LOCAL_SRC_FILES_arm64 := libvoiceunlock_64.a
endif

ifeq ($(strip $(MTK_VOICE_UNLOCK_USE_TAB_LIB)),yes)
LOCAL_MODULE := libvoiceunlocktablet
LOCAL_SRC_FILES_arm := libvoiceunlocktablet_32.a
LOCAL_SRC_FILES_arm64 := libvoiceunlocktablet_32.a
#LOCAL_SRC_FILES_arm64 := libvoiceunlocktablet_64.a
endif

LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_MULTILIB := 32
LOCAL_MODULE_SUFFIX := .a
include $(BUILD_PREBUILT)
endif

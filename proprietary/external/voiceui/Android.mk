ifneq ($(strip $(MTK_VOICE_UI_SUPPORT))_$(strip $(MTK_VOICE_CONTACT_SEARCH_SUPPORT)), no_no)
LOCAL_PATH := $(my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libvoiceui
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_SRC_FILES_arm := libvoiceui_32.a
LOCAL_SRC_FILES_arm64 := libvoiceui_32.a
#LOCAL_SRC_FILES_arm64 := libvoiceui_64.a
LOCAL_MULTILIB := 32
LOCAL_MODULE_SUFFIX := .a
include $(BUILD_PREBUILT)

endif

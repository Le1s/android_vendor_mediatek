ifeq ($(MTK_CHARM_SUPPORT), yes)

LOCAL_PATH:= $(call my-dir)

VER := $(if $(filter eng,$(TARGET_BUILD_VARIANT)),eng,user)

SRC_PATH_32 := ./$(VER)32
SRC_PATH_64 := ./$(VER)64

include $(CLEAR_VARS)
LOCAL_MODULE := libOpenCL
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX :=.so
LOCAL_MULTILIB := both
LOCAL_PROPRIETARY_MODULE := true
LOCAL_SRC_FILES_32 := $(SRC_PATH_32)/libOpenCL.so
LOCAL_SRC_FILES_64 := $(SRC_PATH_64)/libOpenCL.so
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libmtkocl
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX :=.so
LOCAL_MULTILIB := both
LOCAL_PROPRIETARY_MODULE := true
LOCAL_SRC_FILES_32 := $(SRC_PATH_32)/libmtkocl.so
LOCAL_SRC_FILES_64 := $(SRC_PATH_64)/libmtkocl.so
include $(BUILD_PREBUILT)

LIBCLC_HEADERS := $(LOCAL_PATH)/libclc/include

define acp-libclc-headers
  @echo "Copy libclc headers into $(TARGET_OUT_VENDOR)/lib/libclc/"
  @mkdir -p $(TARGET_OUT_VENDOR)/lib/libclc/include
  @$(ACP) -frp $(LIBCLC_HEADERS) $(TARGET_OUT_VENDOR)/lib/libclc/
endef

.PHONY : charm_libclc_headers

charm_libclc_headers : | $(ACP)
	$(call acp-libclc-headers)

include $(CLEAR_VARS)
LOCAL_MODULE := libcharm
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX :=.so
LOCAL_MULTILIB := both
LOCAL_SRC_FILES_32 := $(SRC_PATH_32)/libcharm.so
LOCAL_SRC_FILES_64 := $(SRC_PATH_64)/libcharm.so
LOCAL_ADDITIONAL_DEPENDENCIES := charm_libclc_headers
include $(BUILD_PREBUILT)

endif

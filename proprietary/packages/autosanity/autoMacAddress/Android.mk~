LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES:= \
	$(LOCAL_PATH)/inc/ \
	mediatek/custom/common/factory/inc \
	$(LOCAL_CUST_INC_PATH) \
	$(MTK_PATH_SOURCE)/external/nvram/libnvram

LOCAL_SHARED_LIBRARIES:= libc libcutils libnvram libdl libhwm libaudiocustparam libfile_op

LOCAL_SRC_FILES := src/wifi_mac_address.c
LOCAL_MODULE := wifi_mac
LOCAL_MODULE_PATH := $(TARGET_OUT_EXECUTABLES)
include $(BUILD_EXECUTABLE)


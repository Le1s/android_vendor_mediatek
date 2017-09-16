LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
    BnMtkCodec.cpp \
    CAPEWrapper.cpp

LOCAL_C_INCLUDES:= \
	$(TOP)/frameworks/native/include \
        $(TOP)/$(MTK_ROOT)/external/apedec \
        $(TOP)/$(MTK_ROOT)/frameworks/av/media/libstagefright/include/omx_core


LOCAL_SHARED_LIBRARIES :=       \
        libbinder               \
        libutils                \
        libcutils               \
        libdl                   \
        libui



LOCAL_STATIC_LIBRARIES :=	\
	libapedec_mtk

  
LOCAL_PRELINK_MODULE:= false
LOCAL_MODULE := libBnMtkCodec
LOCAL_MULTILIB := 32

include $(BUILD_SHARED_LIBRARY)

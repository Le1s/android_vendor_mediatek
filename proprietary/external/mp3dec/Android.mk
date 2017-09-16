LOCAL_PATH := $(my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libmp3dec_mtk
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_SRC_FILES_arm := libmp3dec_mtk.a
LOCAL_MODULE_SUFFIX := .a
LOCAL_MULTILIB := 32

#LOCAL_PREBUILT_LIBS += libmp3dec.so \
#                       libmp3frmlen.so \
#                       libdrvb.a

include $(BUILD_PREBUILT)


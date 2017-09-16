ifneq ($(strip $(MTK_PLATFORM)),)
LOCAL_PATH := $(my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
AudioCompFltCustParam.cpp
#AudioCompensationFilter.cpp \

LOCAL_C_INCLUDES := \
	$(MTK_PATH_SOURCE)/external/nvram/libnvram \



LOCAL_SHARED_LIBRARIES := \
    libcustom_nvram \
    libnvram \
    libnativehelper \
    libcutils \
    libutils 
#    libbessound_mtk \

ifeq ($(MTK_STEREO_SPK_ACF_TUNING_SUPPORT),yes)
  LOCAL_CFLAGS += -DMTK_STEREO_SPK_ACF_TUNING_SUPPORT
endif

ifeq ($(MTK_AUDIO_BLOUD_CUSTOMPARAMETER_REV),MTK_AUDIO_BLOUD_CUSTOMPARAMETER_V5)
  LOCAL_CFLAGS += -DMTK_AUDIO_BLOUD_CUSTOMPARAMETER_V5
else
  ifeq ($(strip $(MTK_AUDIO_BLOUD_CUSTOMPARAMETER_REV)),MTK_AUDIO_BLOUD_CUSTOMPARAMETER_V4)
    LOCAL_CFLAGS += -DMTK_AUDIO_BLOUD_CUSTOMPARAMETER_V4
  endif
endif

LOCAL_MODULE := libaudiocompensationfilter

LOCAL_MODULE_TAGS := optional

#ifeq ($(MTK_AUDIO_A64_SUPPORT),yes)
LOCAL_MULTILIB := both
#else
#LOCAL_MULTILIB := 32
#endif

include $(BUILD_SHARED_LIBRARY)
endif

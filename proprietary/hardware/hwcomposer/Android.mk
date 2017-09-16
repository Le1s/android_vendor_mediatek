LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

ifeq ($(MTK_HWC_SUPPORT), yes)

LOCAL_SRC_FILES := \
	hwc.cpp

LOCAL_CFLAGS := \
	-DLOG_TAG=\"hwcomposer\"

ifeq ($(MTK_HDMI_SUPPORT), yes)
LOCAL_CFLAGS += -DMTK_EXTERNAL_SUPPORT
endif

ifeq ($(MTK_WFD_SUPPORT), yes)
LOCAL_CFLAGS += -DMTK_VIRTUAL_SUPPORT
endif

ifneq ($(MTK_PQ_SUPPORT), PQ_OFF)
LOCAL_CFLAGS += -DMTK_ENHAHCE_SUPPORT
endif

ifeq ($(TARGET_FORCE_HWC_FOR_VIRTUAL_DISPLAYS), true)
LOCAL_CFLAGS += -DMTK_FORCE_HWC_COPY_VDS
endif

MTK_HWC_CHIP = $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )

ifneq ($(findstring 1.2, $(MTK_HWC_VERSION)),)
LOCAL_CFLAGS += -DMTK_HWC_VER_1_2
endif

ifneq ($(findstring 1.3, $(MTK_HWC_VERSION)),)
LOCAL_CFLAGS += -DMTK_HWC_VER_1_3
endif

ifneq ($(findstring 1.4, $(MTK_HWC_VERSION)),)
LOCAL_CFLAGS += -DMTK_HWC_VER_1_4
endif

LOCAL_C_INCLUDES += \
	$(TOP)/$(MTK_ROOT)/hardware/include

LOCAL_STATIC_LIBRARIES += \
	hwcomposer.$(TARGET_BOARD_PLATFORM).$(MTK_HWC_VERSION)

LOCAL_SHARED_LIBRARIES := \
	libui \
	libutils \
	libcutils \
	libsync \
	libm4u \
	libion \
	libbwc \
	libion_mtk \
	libdpframework \
	libhardware \
	libgralloc_extra \
	libui_ext \
	libperfservicenative \
	libdl

ifeq ($(MTK_SEC_VIDEO_PATH_SUPPORT), yes)
LOCAL_CFLAGS += -DMTK_SVP_SUPPORT
endif # MTK_SEC_VIDEO_PATH_SUPPORT

ifneq ($(strip $(MTK_BASIC_PACKAGE)),yes)
ifneq ($(TARGET_BUILD_PDK),true)
LOCAL_SHARED_LIBRARIES += \
	libgui_ext
endif
endif

# HAL module implemenation stored in
# hw/<OVERLAY_HARDWARE_MODULE_ID>.<ro.product.board>.so
LOCAL_MODULE := hwcomposer.$(TARGET_BOARD_PLATFORM)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_RELATIVE_PATH := hw
include $(BUILD_SHARED_LIBRARY)

endif # MTK_HWC_SUPPORT

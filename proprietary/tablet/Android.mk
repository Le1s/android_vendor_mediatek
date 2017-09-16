ifneq ($(MTK_TABLET_HARDWARE),)

LOCAL_PATH := $(call my-dir)

#system/lib/hw
include $(CLEAR_VARS)
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/hw
MTK_HWC_CHIP := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )
LOCAL_MODULE := hwcomposer.$(MTK_HWC_CHIP)
POSTFIX := .so
include $(TOP)/vendor/mediatek/proprietary/tablet/symlink.mk

include $(CLEAR_VARS)
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/hw
MTK_HWC_CHIP := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )
LOCAL_MODULE := gralloc.$(MTK_HWC_CHIP)
POSTFIX := .so
include $(TOP)/vendor/mediatek/proprietary/tablet/symlink.mk

include $(CLEAR_VARS)
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/hw
MTK_HWC_CHIP := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )
LOCAL_MODULE := camera.$(MTK_HWC_CHIP)
POSTFIX := .so
include $(TOP)/vendor/mediatek/proprietary/tablet/symlink.mk

include $(CLEAR_VARS)
LOCAL_MODULE_PATH := $(TARGET_ROOT_OUT)
MTK_HWC_CHIP := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )
LOCAL_MODULE := init.$(MTK_HWC_CHIP)
POSTFIX := .rc
include $(TOP)/vendor/mediatek/proprietary/tablet/symlink.mk

include $(CLEAR_VARS)
LOCAL_MODULE_PATH := $(TARGET_ROOT_OUT)
MTK_HWC_CHIP := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )
LOCAL_MODULE := init.$(MTK_HWC_CHIP)
POSTFIX := .usb.rc
include $(TOP)/vendor/mediatek/proprietary/tablet/symlink.mk

include $(CLEAR_VARS)
LOCAL_MODULE_PATH := $(TARGET_ROOT_OUT)
MTK_HWC_CHIP := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )
LOCAL_MODULE := fstab.$(MTK_HWC_CHIP)
POSTFIX :=
include $(TOP)/vendor/mediatek/proprietary/tablet/symlink.mk

include $(CLEAR_VARS)
LOCAL_MODULE_PATH := $(TARGET_RECOVERY_ROOT_OUT)
MTK_HWC_CHIP := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )
LOCAL_MODULE := fstab.$(MTK_HWC_CHIP)
POSTFIX :=
include $(TOP)/vendor/mediatek/proprietary/tablet/symlink.mk

endif

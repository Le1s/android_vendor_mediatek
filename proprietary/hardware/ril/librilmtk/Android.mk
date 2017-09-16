#
# Copyright (C) 2014 MediaTek Inc.
#
# Modification based on code covered by the below mentioned copyright
# and/or permission notice(s).
#

# Copyright 2014 The Android Open Source Project

ifneq ($(GOOGLE_RELEASE_RIL), yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    ril.cpp \
    ril_event.cpp \
    ril_ims.cpp

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libutils \
    libbinder \
    libcutils \
    libhardware_legacy \
    librilutils

LOCAL_STATIC_LIBRARIES := \
    librilmtk-prop-md1

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include

LOCAL_CFLAGS := -DMTK_RIL -DMTK_GEMINI -DANDROID_MULTI_SIM -DMTK_RIL_MD1

ifeq ($(SIM_COUNT), 2)
    LOCAL_CFLAGS += -DANDROID_SIM_COUNT_2
endif

ifeq ($(SIM_COUNT), 3)
    LOCAL_CFLAGS += -DANDROID_SIM_COUNT_3
endif

ifeq ($(SIM_COUNT), 4)
    LOCAL_CFLAGS += -DANDROID_SIM_COUNT_4
endif

ifeq ($(MTK_EAP_SIM_AKA),yes)
  LOCAL_CFLAGS += -DMTK_EAP_SIM_AKA  
endif

ifneq ($(strip $(TARGET_BUILD_VARIANT)), eng)
  LOCAL_CFLAGS += -DFATAL_ERROR_HANDLE
endif

LOCAL_CFLAGS += -DMTK_RIL_MD1

LOCAL_MODULE:= librilmtk

include $(BUILD_SHARED_LIBRARY)

# =========================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    ril.cpp \
    ril_event.cpp \
    ril_ims.cpp

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libutils \
    libbinder \
    libcutils \
    libhardware_legacy \
    librilutils

LOCAL_STATIC_LIBRARIES := \
    librilmtk-prop-md2

LOCAL_CFLAGS := -DMTK_RIL -DMTK_GEMINI -DANDROID_MULTI_SIM -DMTK_RIL_MD2

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include

ifeq ($(SIM_COUNT), 2)
    LOCAL_CFLAGS += -DANDROID_SIM_COUNT_2
endif

ifeq ($(SIM_COUNT), 3)
    LOCAL_CFLAGS += -DANDROID_SIM_COUNT_3
endif

ifeq ($(SIM_COUNT), 4)
    LOCAL_CFLAGS += -DANDROID_SIM_COUNT_4
endif

ifeq ($(MTK_EAP_SIM_AKA),yes)
  LOCAL_CFLAGS += -DMTK_EAP_SIM_AKA  
endif

ifneq ($(strip $(TARGET_BUILD_VARIANT)), eng)
  LOCAL_CFLAGS += -DFATAL_ERROR_HANDLE
endif

LOCAL_CFLAGS += -DMTK_RIL_MD2

LOCAL_MODULE:= librilmtkmd2

include $(BUILD_SHARED_LIBRARY)
# For RdoServD which needs a static library
# =========================================
ifneq ($(ANDROID_BIONIC_TRANSITION),)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    ril.cpp \
    ril_ims.cpp

LOCAL_STATIC_LIBRARIES := \
    libutils_static \
    libcutils \
    librilutils_static

LOCAL_CFLAGS :=

LOCAL_MODULE:= libril_static

include $(BUILD_STATIC_LIBRARY)
endif # ANDROID_BIONIC_TRANSITION
endif # ($(GOOGLE_RELEASE_RIL),yes)

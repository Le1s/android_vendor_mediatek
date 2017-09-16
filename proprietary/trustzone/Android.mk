# Copyright Statement:
#
# This software/firmware and related documentation ("MediaTek Software") are
# protected under relevant copyright laws. The information contained herein
# is confidential and proprietary to MediaTek Inc. and/or its licensors.
# Without the prior written permission of MediaTek inc. and/or its licensors,
# any reproduction, modification, use or disclosure of MediaTek Software,
# and information contained herein, in whole or in part, shall be strictly prohibited.
#
# MediaTek Inc. (C) 2010. All rights reserved.
#
# BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
# THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
# RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
# AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
# NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
# SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
# SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
# THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
# THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
# CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
# SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
# STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
# CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
# AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
# OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
# MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
#
# The following software/firmware and/or related documentation ("MediaTek Software")
# have been modified by MediaTek Inc. All revisions are subject to any receiver's
# applicable license agreements with MediaTek Inc.

ifneq ($(strip $(MTK_PROJECT_NAME)),)

LOCAL_PATH := $(call my-dir)
# *************************************************************************
# Set shell align with Android build system
# *************************************************************************
SHELL        := /bin/bash
MTK_PROJECT_NAME := $(subst full_,,$(TARGET_PRODUCT))
#MTK_PROJECT_FOLDER := $(shell find device/* -maxdepth 1 -name $(MTK_PROJECT_NAME))
MTK_TRUSTZONE_CFG_FOLDER := vendor/mediatek/proprietary/trustzone/project
MKTOPDIR := $(shell pwd -P)
MTK_PLATFORM_LC := $(shell echo $(MTK_PLATFORM) | tr A-Z a-z )
#PRJ_MF := $(MTK_PROJECT_FOLDER)/ProjectConfig.mk
PRJ_COMMON := $(MTK_TRUSTZONE_CFG_FOLDER)/common.mk
PRJ_CHIP := $(MTK_TRUSTZONE_CFG_FOLDER)/$(MTK_PLATFORM_LC).mk
PRJ_BASE := $(MTK_TRUSTZONE_CFG_FOLDER)/$(MTK_BASE_PROJECT).mk
PRJ_MF := $(MTK_TRUSTZONE_CFG_FOLDER)/$(MTK_TARGET_PROJECT).mk
ifneq ($(wildcard $(PRJ_COMMON)),)
$(info including $(PRJ_COMMON))
include $(PRJ_COMMON)
endif
ifneq ($(wildcard $(PRJ_CHIP)),)
$(info including $(PRJ_CHIP))
include $(PRJ_CHIP)
endif
ifneq ($(wildcard $(PRJ_BASE)),)
$(info including $(PRJ_BASE))
include $(PRJ_BASE)
endif
ifneq ($(wildcard $(PRJ_MF)),)
$(info including $(PRJ_MF))
include $(PRJ_MF)
endif
hide := @

SHOWTIMECMD :=  date "+%Y/%m/%d %H:%M:%S"
SHOWTIME :=  $(shell $(SHOWTIMECMD))
OUT_DIR ?=  out
LOG_DIR := $(OUT_DIR)/target/product/
DEAL_STDOUT := > $(LOG_DIR)$(MTK_PROJECT_NAME)_trustzone.log 2>&1

TRUST_TEE_WD  :=  vendor/mediatek/proprietary/trustzone
ATF_BUILD_SCRIPT := vendor/arm/atf/build.sh
TEE_BUILD_SCRIPT := vendor/trustonic/platform/$(PLATFORM)/t-base/build.sh
TZ_BUILD_SCRIPT := $(TRUST_TEE_WD)/build.sh
TRUST_TEE_IMAGES := $(OUT_DIR)/target/product/$(MTK_PROJECT_NAME)/trustzone/bin/trustzone.bin
export ARCH_MTK_PLATFORM := $(MTK_PLATFORM_LC)
export MTK_PROJECT := $(MTK_PROJECT_NAME)

TRUSTZONE_PARTITION := no

ifeq ($(MTK_ATF_SUPPORT),yes)
  TRUSTZONE_PARTITION := yes
endif
ifeq ($(MTK_TEE_SUPPORT),yes)
  TRUSTZONE_PARTITION := yes
endif

ifeq ($(TRUSTZONE_PARTITION),yes)
  export MTK_ATF_SUPPORT
  export MTK_TEE_SUPPORT
  export TRUSTONIC_TEE_SUPPORT
  export MTK_ATF_VERSION
  export MKTOPDIR
  export TARGET_BUILD_VARIANT
  export OUT_DIR
endif

$(info TARGET_PRODUCT is $(TARGET_PRODUCT))
$(info MTK_PROJECT_NAME is $(MTK_PROJECT_NAME))
$(info TARGET_BUILD_TYPE is $(TARGET_BUILD_TYPE))
$(info MKTOPDIR is $(MKTOPDIR))
$(info JAVA_HOME is $(JAVA_HOME))
$(info MTK_PROJECT is $(MTK_PROJECT))
$(info TARGET_BUILD_VARIANT is $(TARGET_BUILD_VARIANT))
$(info OUT_DIR is $(OUT_DIR))
$(info MTK_ATF_SUPPORT is $(MTK_ATF_SUPPORT))
$(info MTK_TEE_SUPPORT is $(MTK_TEE_SUPPORT))
$(info TRUSTONIC_TEE_SUPPORT is $(TRUSTONIC_TEE_SUPPORT))
$(info TRUSTZONE_PARTITION is $(TRUSTZONE_PARTITION))
$(info MTK_ATF_VERSION is $(MTK_ATF_VERSION))
$(info MTK_IN_HOUSE_TEE_SUPPORT is $(MTK_IN_HOUSE_TEE_SUPPORT))

ifeq ($(MTK_IN_HOUSE_TEE_SUPPORT),yes)
include $(call all-makefiles-under,$(LOCAL_PATH))
trustzone: tz.img
endif

ifneq ($(MTK_TEE_SUPPORT),yes)
trustzone:
else
trustzone: mcDriverDaemon
endif
ifeq ($(TRUSTZONE_PARTITION),yes)
  ifneq ($(wildcard $(TRUST_TEE_WD)/build.sh),)
	$(hide) echo $(SHOWTIME) $@ ing ...
	$(hide) echo -e \\t\\t\\t\\b\\b\\b\\bLOG: $(LOG_DIR)$(MTK_PROJECT_NAME)_$@.log
	$(hide) rm -f $(LOG_DIR)$(MTK_PROJECT_NAME)_$@.log $(LOG_DIR)$(MTK_PROJECT_NAME)_$@.log_err
	$(hide) $(SHELL) $(TZ_BUILD_SCRIPT) $(MTK_PROJECT_NAME) $(DEAL_STDOUT)
	$(hide) cp -f $(TRUST_TEE_IMAGES) $(LOG_DIR)/$(MTK_PROJECT_NAME)/
  else
	$(hide) echo Not find $(TRUST_TEE_WD)/build.sh, skip $@.
  endif
else
	$(hide) echo Not support $@.
endif

droidcore: trustzone

endif

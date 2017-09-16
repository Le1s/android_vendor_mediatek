#!/bin/bash
# Copyright Statement:
#
# This software/firmware and related documentation ("MediaTek Software") are
# protected under relevant copyright laws. The information contained herein
# is confidential and proprietary to MediaTek Inc. and/or its licensors.
# Without the prior written permission of MediaTek inc. and/or its licensors,
# any reproduction, modification, use or disclosure of MediaTek Software,
# and information contained herein, in whole or in part, shall be strictly prohibited.

# MediaTek Inc. (C) 2014. All rights reserved.
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

##############################################################
# Program:
# Program to create ARM trusted firmware and tee binary
#

### temp workaround for AOSP ###
if [ "${TARGET_PRODUCT}" = "" ]; then
    echo "TARGET_PRODUCT is not set! It should be set to full project name!"
    exit 1
fi
if [ "${TARGET_DEVICE}" = "" ]; then
    echo "TARGET_DEVICE is not set! It should be set to base project name!"
    exit 1
fi
if [ "${MTK_PLATFORM}" = "" ]; then
    echo "MTK_PLATFORM is not set!"
    exit 1
fi

export MKTOPDIR="`pwd`/../../../../"
if [ "${OUT_DIR}" = "" ]; then
OUT_DIR=${MKTOPDIR}/out
fi
export MTK_PROJECT=${TARGET_PRODUCT}
MTK_PLATFORM_LC=`echo ${MTK_PLATFORM} | tr A-Z a-z`
PRJ_COMMON=project/common.mk
PRJ_CHIP=project/${MTK_PLATFORM_LC}.mk
PRJ_MF=project/${TARGET_DEVICE}.mk
if [ -f ${PRJ_COMMON} ]; then
  echo "including ${PRJ_COMMON}"
  source ${PRJ_COMMON}
fi
if [ -f ${PRJ_CHIP} ]; then
  echo "including ${PRJ_CHIP}"
  source ${PRJ_CHIP}
fi
if [ -f ${PRJ_MF} ]; then
  echo "including ${PRJ_MF}"
  source ${PRJ_MF}
fi
export ARCH_MTK_PLATFORM=${MTK_PLATFORM_LC}

ABS_OUT_DIR=$( cd $(readlink -f ${OUT_DIR}) ; pwd -P )
TRUSTONZE_IMAGE_NAME=${ABS_OUT_DIR}/target/product/${MTK_PROJECT}/trustzone/bin/trustzone.bin
TRUSTONZE_TARGET=${ABS_OUT_DIR}/target/product/${MTK_PROJECT}/trustzone.bin

TRUSTZONE_PARTITION=no

if [ "${MTK_ATF_SUPPORT}" = "yes" ]; then
  TRUSTZONE_PARTITION=yes
fi
if [ "${MTK_TEE_SUPPORT}" = "yes" ]; then
  TRUSTZONE_PARTITION=yes
fi

echo "MTK_ATF_SUPPORT is ${MTK_ATF_SUPPORT}"
echo "MTK_TEE_SUPPORT is ${MTK_TEE_SUPPORT}"
echo "TRUSTONIC_TEE_SUPPORT is ${TRUSTONIC_TEE_SUPPORT}"
echo "TRUSTZONE_PARTITION is ${TRUSTZONE_PARTITION}"
echo "MTK_ATF_VERSION is ${MTK_ATF_VERSION}"
echo "ABS_OUT_DIR is ${ABS_OUT_DIR}"

if [ "${TRUSTZONE_PARTITION}" = "yes" ]; then
  export MTK_ATF_SUPPORT
  export MTK_TEE_SUPPORT
  export TRUSTONIC_TEE_SUPPORT
  export MTK_ATF_VERSION
  export OUT_DIR
  ./build.sh
  cp -f ${TRUSTONZE_IMAGE_NAME} ${TRUSTONZE_TARGET}
else
  echo "Trustzone configurations are not enabled!"
fi

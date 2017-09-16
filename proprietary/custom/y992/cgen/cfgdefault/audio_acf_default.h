/*****************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2008
*
*  BY OPENING THIS FILE, BUYER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
*  THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
*  RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO BUYER ON
*  AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
*  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
*  NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
*  SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
*  SUPPLIED WITH THE MEDIATEK SOFTWARE, AND BUYER AGREES TO LOOK ONLY TO SUCH
*  THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. MEDIATEK SHALL ALSO
*  NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE RELEASES MADE TO BUYER'S
*  SPECIFICATION OR TO CONFORM TO A PARTICULAR STANDARD OR OPEN FORUM.
*
*  BUYER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND CUMULATIVE
*  LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
*  AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
*  OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY BUYER TO
*  MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*
*  THE TRANSACTION CONTEMPLATED HEREUNDER SHALL BE CONSTRUED IN ACCORDANCE
*  WITH THE LAWS OF THE STATE OF CALIFORNIA, USA, EXCLUDING ITS CONFLICT OF
*  LAWS PRINCIPLES.  ANY DISPUTES, CONTROVERSIES OR CLAIMS ARISING THEREOF AND
*  RELATED THERETO SHALL BE SETTLED BY ARBITRATION IN SAN FRANCISCO, CA, UNDER
*  THE RULES OF THE INTERNATIONAL CHAMBER OF COMMERCE (ICC).
*
*****************************************************************************/

/*******************************************************************************
 *
 * Filename:
 * ---------
 * audio_acf_default.h
 *
 * Project:
 * --------
 *   ALPS
 *
 * Description:
 * ------------
 * This file is the header of audio customization related parameters or definition.
 *
 * Author:
 * -------
 * Tina Tsai
 *
 *============================================================================
 *             HISTORY
 * Below this line, this part is controlled by CC/CQ. DO NOT MODIFY!!
 *------------------------------------------------------------------------------
 * $Revision:$
 * $Modtime:$
 * $Log:$
 *
 *
 *
 *
 *------------------------------------------------------------------------------
 * Upper this line, this part is controlled by CC/CQ. DO NOT MODIFY!!
 *============================================================================
 ****************************************************************************/
#ifndef AUDIO_ACF_DEFAULT_H
#define AUDIO_ACF_DEFAULT_H
#if defined(MTK_AUDIO_BLOUD_CUSTOMPARAMETER_V5)
#define BES_LOUDNESS_ACF_L_HPF_FC       300
#define BES_LOUDNESS_ACF_L_HPF_ORDER    4
#define BES_LOUDNESS_ACF_L_BPF_FC       800,1500,0,0,0,0,0,0
#define BES_LOUDNESS_ACF_L_BPF_BW       1000,1000,0,0,0,0,0,0
#define BES_LOUDNESS_ACF_L_BPF_GAIN     -1533,-1535,0,0,0,0,0,0
#define BES_LOUDNESS_ACF_L_LPF_FC       8000
#define BES_LOUDNESS_ACF_L_LPF_ORDER    1
#define BES_LOUDNESS_ACF_R_HPF_FC       0
#define BES_LOUDNESS_ACF_R_HPF_ORDER    0
#define BES_LOUDNESS_ACF_R_BPF_FC       0,0,0,0,0,0,0,0
#define BES_LOUDNESS_ACF_R_BPF_BW       0,0,0,0,0,0,0,0
#define BES_LOUDNESS_ACF_R_BPF_GAIN     0,0,0,0,0,0,0,0
#define BES_LOUDNESS_ACF_R_LPF_FC       0
#define BES_LOUDNESS_ACF_R_LPF_ORDER    0

#define BES_LOUDNESS_ACF_SEP_LR_FILTER  0

#define BES_LOUDNESS_ACF_WS_GAIN_MAX    0
#define BES_LOUDNESS_ACF_WS_GAIN_MIN    0
#define BES_LOUDNESS_ACF_FILTER_FIRST   0

#define BES_LOUDNESS_ACF_NUM_BANDS      0
#define BES_LOUDNESS_ACF_FLT_BANK_ORDER 0
#define BES_LOUDNESS_ACF_DRC_DELAY      0
#define BES_LOUDNESS_ACF_CROSSOVER_FREQ 0, 0, 0, 0, 0, 0, 0
#define BES_LOUDNESS_ACF_SB_MODE        0, 0, 0, 0, 0, 0, 0, 0
#define BES_LOUDNESS_ACF_SB_GAIN        0, 0, 0, 0, 0, 0, 0, 0
#define BES_LOUDNESS_ACF_GAIN_MAP_IN    \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0
#define BES_LOUDNESS_ACF_GAIN_MAP_OUT   \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0,                  \
        0, 0, 0, 0, 0
#define BES_LOUDNESS_ACF_ATT_TIME       \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0
#define BES_LOUDNESS_ACF_REL_TIME       \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0
#define BES_LOUDNESS_ACF_HYST_TH        \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0,               \
        0, 0, 0, 0, 0, 0

#define BES_LOUDNESS_ACF_LIM_TH     0
#define BES_LOUDNESS_ACF_LIM_GN     0
#define BES_LOUDNESS_ACF_LIM_CONST  0
#define BES_LOUDNESS_ACF_LIM_DELAY  0
#else
   /* Compensation Filter HSF coeffs: default all pass filter       */
    /* BesLoudness also uses this coeffs    */ 
#define BES_LOUDNESS_HSF_COEFF \
0x7468827,   0xf172efb1,   0x7468827,   0x7416cb45,   0x0,     \
0x73743a9,   0xf19178ae,   0x73743a9,   0x7314cc2b,   0x0,     \
0x6f26907,   0xf21b2df2,   0x6f26907,   0x6e77d029,   0x0,     \
0x6a35720,   0xf2b951bf,   0x6a35720,   0x690bd4a0,   0x0,     \
0x6887eee,   0xf2ef0223,   0x6887eee,   0x672cd61c,   0x0,     \
0x612a8d2,   0xf3daae5c,   0x612a8d2,   0x5ec4dc6f,   0x0,     \
0x59191f2,   0xf4dcdc1b,   0x59191f2,   0x5530e2fe,   0x0,     \
0x56730a0,   0xf5319ebf,   0x56730a0,   0x51f4e50e,   0x0,     \
0x4b55da1,   0xf69544be,   0x4b55da1,   0x43e6ed3a,   0x0,     \
    \
0x7abd80b,   0xf0a84fea,   0x7abd80b,   0x7a67c4ec,   0x0,     \
0x7a439e7,   0xf0b78c32,   0x7a439e7,   0x79ddc556,   0x0,     \
0x78078fb,   0xf0ff0e09,   0x78078fb,   0x774ac73a,   0x0,     \
0x75478f8,   0xf1570e10,   0x75478f8,   0x73fec96f,   0x0,     \
0x744c9fc,   0xf1766c08,   0x744c9fc,   0x72c9ca30,   0x0,     \
0x6fb3f53,   0xf2098159,   0x6fb3f53,   0x6cf2cd8a,   0x0,     \
0x6a16750,   0xf2bd3160,   0x6a16750,   0x656fd142,   0x0,     \
0x681ae6b,   0xf2fca32a,   0x681ae6b,   0x62b1d27c,   0x0,     \
0x5effcff,   0xf4200601,   0x5effcff,   0x559fd79f,   0x0
   

    /* Compensation Filter BPF coeffs: default all pass filter      */ 
#define BES_LOUDNESS_BPF_COEFF \
0x43f19802,   0x342267fd,   0xc7ec0000,     \
0x44459b75,   0x3324648a,   0xc8950000,     \
0x45c6adb2,   0x2e9f524d,   0xcb9a0000,     \
0x478ac793,   0x294d386c,   0xcf270000,     \
0x4827d15e,   0x27752ea1,   0xd0620000,     \
0x4af30000,   0x1f0b0000,   0xd6010000,     \
    \
0x3f2f8389,   0x3d8c7c76,   0xc3440000,     \
0x3f1d83dd,   0x3d567c22,   0xc38c0000,     \
0x3ecb8570,   0x3c5e7a8f,   0xc4d60000,     \
0x3e69876d,   0x3b377892,   0xc65f0000,     \
0x3e468826,   0x3ad077d9,   0xc6e80000,     \
0x3dab8ba5,   0x38fd745a,   0xc9560000,     \
    \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
    \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
    \
0x43f18c03,   0x342273fc,   0xc7ec0000,     \
0x44458d66,   0x33247299,   0xc8950000,     \
0x45c69476,   0x2e9f6b89,   0xcb9a0000,     \
0x478a9e45,   0x294d61ba,   0xcf270000,     \
0x4827a206,   0x27755df9,   0xd0620000,     \
0x4af3b50c,   0x1f0b4af3,   0xd6010000,     \
    \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
    \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
    \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0

#define BES_LOUDNESS_LPF_COEFF \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0,     \
0x0,   0x0,   0x0
#define BES_LOUDNESS_WS_GAIN_MAX  0x0
#define BES_LOUDNESS_WS_GAIN_MIN  0x0
#define BES_LOUDNESS_FILTER_FIRST  0x0
#define BES_LOUDNESS_ATT_TIME  0xa4
#define BES_LOUDNESS_REL_TIME  0x4010
#define BES_LOUDNESS_GAIN_MAP_IN \
0xc4, 0xd0, 0xe7, 0xe8, 0x0
#define BES_LOUDNESS_GAIN_MAP_OUT \
0x12, 0x12, 0x12, 0x12, 0x0
#endif
#endif

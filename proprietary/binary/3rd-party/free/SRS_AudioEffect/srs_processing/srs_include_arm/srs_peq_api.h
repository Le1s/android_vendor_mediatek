/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  ParametricEQ public APIs header file
 *
 *	Author: Oscar Huang
 *	
 *	(RCS keywords below, do not edit)
 *  $Id: //srstech/srs_common/std_fxp/include/srs_parametriceq_api.h#6 $
 *  $Author: oscarh $
 *  $Date: 2011/02/14 $
********************************************************************************/

#ifndef __SRS_PARAMETRICEQ_API_H__
#define __SRS_PARAMETRICEQ_API_H__

#include "srs_typedefs.h"
#include "srs_fxp.h"
#include "srs_peq_def.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

/*APIs*/
int			SRS_PEQ_GetObjSize(const SRSPeqCreateData *pAttr);

SRSResult	SRS_PEQ_CreateObj(SRSPeqObj *pObj, void *pBuf, SRS_Allocator alloc, void* pContext, const SRSPeqCreateData *pAttr);

SRSResult	SRS_PEQ_InitObj(SRSPeqObj peqObj, const SRSPeqFilterCfg *filterCfg);

void		SRS_PEQ_SetControlDefaults(SRSPeqObj peqObj);

SRSResult	SRS_PEQ_Process(SRSPeqObj peqObj, SRSInt32 *audioChan, int blockSamples);

void		SRS_PEQ_SetEnable(SRSPeqObj peqObj, SRSBool enable);
SRSResult	SRS_PEQ_SetInputGain(SRSPeqObj peqObj, SRSInt16 gain);	//I16.SRS_PEQ_GAIN_IWL
SRSResult	SRS_PEQ_SetOutputGain(SRSPeqObj peqObj, SRSInt16 gain);//I16.SRS_PEQ_GAIN_IWL
SRSResult	SRS_PEQ_SetBypassGain(SRSPeqObj peqObj, SRSInt16 gain);//I16.SRS_PEQ_GAIN_IWL

SRSResult	SRS_PEQ_SetControls(SRSPeqObj peqObj, const SRSPeqControls *pCtrls);

SRSBool		SRS_PEQ_GetEnable(SRSPeqObj peqObj);
SRSInt16	SRS_PEQ_GetInputGain(SRSPeqObj peqObj);
SRSInt16	SRS_PEQ_GetOutputGain(SRSPeqObj peqObj);
SRSInt16	SRS_PEQ_GetBypassGain(SRSPeqObj peqObj);

void		SRS_PEQ_GetControls(SRSPeqObj peqObj, SRSPeqControls *pCtrls);

/*version info query function*/
unsigned char	SRS_PEQ_GetTechVersion(SRSVersion which);
unsigned char	SRS_PEQ_GetLibVersion(SRSVersion which);

const SRSLibInfo*  SRS_PEQ_GetLibsInfo();

#ifdef __cplusplus
}
#endif /*__cplusplus*/


#endif /*__SRS_PARAMETRICEQ_API_H__*/

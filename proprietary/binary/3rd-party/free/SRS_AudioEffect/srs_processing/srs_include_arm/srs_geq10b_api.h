/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes ANSI 10-band graphic EQ APIs
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_graphiceq/std_fxp/include/srs_geq10b_api.h#6 $
 *  $Author: oscarh $
 *  $Date: 2010/11/16 $
 *	
********************************************************************************/

#ifndef __SRS_GEQ10B_API_H__
#define __SRS_GEQ10B_API_H__

#include "srs_geq_def.h"
#include "srs_geq_ver_api.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:

int				SRS_GEQ10B_GetObjSize();
SRSResult		SRS_GEQ10B_CreateObj(SRSGeq10bObj *pObj, void *pBuf, SRS_Allocator alloc, void *pContext);

SRSResult		SRS_GEQ10B_InitObj8k(SRSGeq10bObj geqObj);
SRSResult		SRS_GEQ10B_InitObj11k(SRSGeq10bObj geqObj);
SRSResult		SRS_GEQ10B_InitObj16k(SRSGeq10bObj geqObj);
SRSResult		SRS_GEQ10B_InitObj22k(SRSGeq10bObj geqObj);
SRSResult		SRS_GEQ10B_InitObj24k(SRSGeq10bObj geqObj);
SRSResult		SRS_GEQ10B_InitObj32k(SRSGeq10bObj geqObj);
SRSResult		SRS_GEQ10B_InitObj44k(SRSGeq10bObj geqObj);
SRSResult		SRS_GEQ10B_InitObj48k(SRSGeq10bObj geqObj);

void			SRS_GEQ10B_SetControlDefaults(SRSGeq10bObj geqObj);


void			SRS_GEQ10B_SetEnable(SRSGeq10bObj geqObj, SRSBool enable);
SRSBool			SRS_GEQ10B_GetEnable(SRSGeq10bObj geqObj);

SRSResult		SRS_GEQ10B_SetBandGain(SRSGeq10bObj geqObj, int bandIndex, SRSInt16 gain);
SRSInt16		SRS_GEQ10B_GetBandGain(SRSGeq10bObj geqObj, int bandIndex);

void			SRS_GEQ10B_SetLimiterEnable(SRSGeq10bObj geqObj, SRSBool enable);
SRSBool			SRS_GEQ10B_GetLimiterEnable(SRSGeq10bObj geqObj);

SRSResult		SRS_GEQ10B_Process(SRSGeq10bObj geqObj, SRSInt32 *audioIO, int blockFrames, void *ws);


#ifdef __cplusplus
}
#endif /*__cplusplus*/


/////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////


#endif /*__SRS_GEQ10B_API_H__*/

/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes 5-band graphic EQ APIs
 *
 *  RCS keywords:
 *	$Id $
 *  $Author: oscarh $
 *  $Date: 2010/11/16 $
 *	
********************************************************************************/

#ifndef __SRS_GEQ5B_API_H__
#define __SRS_GEQ5B_API_H__

#include "srs_geq_def.h"
#include "srs_geq_ver_api.h"

#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:
int				SRS_GEQ5B_GetObjSize();
SRSResult		SRS_GEQ5B_CreateObj(SRSGeq5bObj *pObj, void *pBuf, SRS_Allocator alloc, void *pContext);

SRSResult		SRS_GEQ5B_InitObj8k(SRSGeq5bObj geqObj);
SRSResult		SRS_GEQ5B_InitObj11k(SRSGeq5bObj geqObj);
SRSResult		SRS_GEQ5B_InitObj16k(SRSGeq5bObj geqObj);
SRSResult		SRS_GEQ5B_InitObj22k(SRSGeq5bObj geqObj);
SRSResult		SRS_GEQ5B_InitObj24k(SRSGeq5bObj geqObj);
SRSResult		SRS_GEQ5B_InitObj32k(SRSGeq5bObj geqObj);
SRSResult		SRS_GEQ5B_InitObj44k(SRSGeq5bObj geqObj);
SRSResult		SRS_GEQ5B_InitObj48k(SRSGeq5bObj geqObj);

void			SRS_GEQ5B_SetControlDefaults(SRSGeq5bObj geqObj);

SRSResult		SRS_GEQ5B_SetExtraBandBehavior(SRSGeq5bObj geqObj, SRS5BandGeqExtraBandBehavior behavior);
SRS5BandGeqExtraBandBehavior	SRS_GEQ5B_GetExtraBandBehavior(SRSGeq5bObj geqObj);

void			SRS_GEQ5B_SetEnable(SRSGeq5bObj geqObj, SRSBool enable);
SRSBool			SRS_GEQ5B_GetEnable(SRSGeq5bObj geqObj);

SRSResult		SRS_GEQ5B_SetBandGain(SRSGeq5bObj geqObj, int bandIndex, SRSInt16 gain);
SRSInt16		SRS_GEQ5B_GetBandGain(SRSGeq5bObj geqObj, int bandIndex);

void			SRS_GEQ5B_SetLimiterEnable(SRSGeq5bObj geqObj, SRSBool enable);
SRSBool			SRS_GEQ5B_GetLimiterEnable(SRSGeq5bObj geqObj);

SRSResult		SRS_GEQ5B_Process(SRSGeq5bObj geqObj, SRSInt32 *audioIO, int blockFrames, void *ws);


#ifdef __cplusplus
}
#endif /*__cplusplus*/


/////////////////////////////////////////////////////////////////////////////////////////////////////////



#endif /*__SRS_5BAND_GRAPHICEQ_API_H__*/

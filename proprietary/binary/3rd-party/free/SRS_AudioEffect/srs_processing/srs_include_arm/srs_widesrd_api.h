/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_widesurround APIs
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/

#ifndef __SRS_WIDESURROUND_API_H__
#define __SRS_WIDESURROUND_API_H__

#include "srs_widesrd_def.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

int					SRS_WideSrd_GetObjSize();
SRSResult			SRS_WideSrd_CreateObj(SRSWideSrdObj *pObj, void *pBuf, SRS_Allocator alloc, void *pContext);

SRSResult			SRS_WideSrd_InitObj32k(SRSWideSrdObj wsObj);
SRSResult			SRS_WideSrd_InitObj44k(SRSWideSrdObj wsObj);
SRSResult			SRS_WideSrd_InitObj48k(SRSWideSrdObj wsObj);

SRSResult			SRS_WideSrd_Process(SRSWideSrdObj wsObj, SRSStereoCh *audioIO, void *wksps);

//"Set" functions:
void				SRS_WideSrd_SetControlDefaults(SRSWideSrdObj wsObj);
SRSResult			SRS_WideSrd_SetEnable(SRSWideSrdObj wsObj, SRSBool enable);
SRSResult			SRS_WideSrd_SetInputGain(SRSWideSrdObj wsObj, SRSInt16 gain); //I16.SRS_WIDESRD_GAIN_IWL
SRSResult			SRS_WideSrd_SetBypassGain(SRSWideSrdObj wsObj, SRSInt16 gain); //I16.SRS_WIDESRD_GAIN_IWL
SRSResult			SRS_WideSrd_SetCenterBoostGain(SRSWideSrdObj wsObj, SRSInt16 gain); //I16.SRS_WIDESRD_GAIN_IWL
SRSResult			SRS_WideSrd_SetSpeakerSeparationLevel(SRSWideSrdObj wsObj, SRSInt16 separation); //I16.SRS_WIDESRD_SPK_SEP_IWL
SRSResult			SRS_WideSrd_SetHsHPFEnable(SRSWideSrdObj wsObj, SRSBool enable);

SRSResult			SRS_WideSrd_SetControls(SRSWideSrdObj wsObj, const SRSWideSrdControls *pCtrls);

//"Get" functions:
SRSBool				SRS_WideSrd_GetEnable(SRSWideSrdObj wsObj);
SRSInt16			SRS_WideSrd_GetInputGain(SRSWideSrdObj wsObj);
SRSInt16			SRS_WideSrd_GetBypassGain(SRSWideSrdObj wsObj); 
SRSInt16			SRS_WideSrd_GetCenterBoostGain(SRSWideSrdObj wsObj);
SRSInt16			SRS_WideSrd_GetSpeakerSeparationLevel(SRSWideSrdObj wsObj);
SRSBool				SRS_WideSrd_GetHsHPFEnable(SRSWideSrdObj wsObj);

void				SRS_WideSrd_GetControls(SRSWideSrdObj wsObj, SRSWideSrdControls *pCtrls);

//Version functions:
unsigned char		SRS_WideSrd_GetLibVersion(SRSVersion which);
unsigned char		SRS_WideSrd_GetTechVersion(SRSVersion which);

const SRSLibInfo*	SRS_WideSrd_GetLibsInfo();

#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif /*__SRS_WIDESURROUND_API_H__*/

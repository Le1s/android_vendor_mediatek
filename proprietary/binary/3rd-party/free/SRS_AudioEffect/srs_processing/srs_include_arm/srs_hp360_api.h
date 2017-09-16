/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_hp360 APIs
 *
 *	Author: Zesen Zhuang
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_hp360/std_fxp/include/srs_hp360_api.h#6 $
 *  $Author: zesenz $
 *  $Date: 2011/01/20 $
 *	
********************************************************************************/

#ifndef __SRS_HP360_API_H__
#define __SRS_HP360_API_H__

//#include "srs_typedefs.h"
#include "srs_fxp.h"
#include "srs_hp360_def.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:
int			SRS_Hp360_GetObjSize();
SRSResult	SRS_Hp360_CreateObj(SRSHp360Obj *pObj, void *pBuf, SRS_Allocator alloc,void* pContext);

SRSResult	SRS_Hp360_InitObj8k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj11k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj16k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj22k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj24k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj32k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj44k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj48k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj88k(SRSHp360Obj hp360Obj);
SRSResult	SRS_Hp360_InitObj96k(SRSHp360Obj hp360Obj);


/* main APIs */
/* the supported block size should be bigger than 25 */
SRSResult	SRS_Hp360_Process(SRSHp360Obj hp360Obj, SRS5Point1Ch *input, SRSStereoCh *output, int blockSamples, void *ws);

//"Set" functions:
void		SRS_Hp360_SetControlDefaults(SRSHp360Obj hp360Obj);
void		SRS_Hp360_SetEnable(SRSHp360Obj hp360Obj, SRSBool enable);
SRSResult	SRS_Hp360_SetInputGain(SRSHp360Obj hp360Obj, SRSInt16 gain);
SRSResult	SRS_Hp360_SetOutputGain(SRSHp360Obj hp360Obj, SRSInt16 gain);
SRSResult	SRS_Hp360_SetBypassGain(SRSHp360Obj hp360Obj, SRSInt16 gain);

//"Get" functions:
SRSBool		SRS_Hp360_GetEnable(SRSHp360Obj hp360Obj);
SRSInt16	SRS_Hp360_GetInputGain(SRSHp360Obj hp360Obj);
SRSInt16	SRS_Hp360_GetOutputGain(SRSHp360Obj hp360Obj);
SRSInt16	SRS_Hp360_GetBypassGain(SRSHp360Obj hp360Obj);

/* version info query function */
unsigned char	SRS_Hp360_GetTechVersion(SRSVersion which);
unsigned char	SRS_Hp360_GetLibVersion(SRSVersion which);

#ifdef __cplusplus
}
#endif /*__cplusplus*/



#endif /*__SRS_HP360_API_H__*/

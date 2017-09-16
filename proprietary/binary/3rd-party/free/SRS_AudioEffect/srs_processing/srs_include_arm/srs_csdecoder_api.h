/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_csdecoder APIs
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/

#ifndef __SRS_CSDECODER_API_H__
#define __SRS_CSDECODER_API_H__

/*Data type definition here:*/

#include "srs_typedefs.h"
#include "srs_fxp.h"
#include "srs_csdecoder_def.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:

int				SRS_CSD_GetObjSize();
SRSResult		SRS_CSD_CreateObj(SRSCSDObj *pObj, void* pBuf, SRS_Allocator alloc, void *pContext);

SRSResult		SRS_CSD_InitObj16k(SRSCSDObj csdObj);
SRSResult		SRS_CSD_InitObj22k(SRSCSDObj csdObj);
SRSResult		SRS_CSD_InitObj24k(SRSCSDObj csdObj);
SRSResult		SRS_CSD_InitObj32k(SRSCSDObj csdObj);
SRSResult		SRS_CSD_InitObj44k(SRSCSDObj csdObj);
SRSResult		SRS_CSD_InitObj48k(SRSCSDObj csdObj);
SRSResult		SRS_CSD_InitObj64k(SRSCSDObj csdObj);
SRSResult		SRS_CSD_InitObj88k(SRSCSDObj csdObj);
SRSResult		SRS_CSD_InitObj96k(SRSCSDObj csdObj);

void			SRS_CSD_SetControlDefaults(SRSCSDObj csdObj);

SRSResult		SRS_CSD_Process(SRSCSDObj csdObj, SRSStereoCh* Input, SRS6Point1Ch* Output, int blockSize, void* workspace);

//"Set" functions:
SRSResult		SRS_CSD_SetInputGain(SRSCSDObj csdObj,				SRSInt16 inputGain);
SRSResult		SRS_CSD_SetProcMode(SRSCSDObj csdObj,				SRSCSDProcMode mode);
SRSResult		SRS_CSD_SetOutMode(SRSCSDObj csdObj,				SRSCSDOutMode outputmode);
void			SRS_CSD_SetPhantomEnable(SRSCSDObj csdObj,			SRSBool enable);
void			SRS_CSD_SetCenterFullBandEnable(SRSCSDObj csdObj,	SRSBool enable);


//"Get" functions:
SRSInt16		SRS_CSD_GetInputGain(SRSCSDObj csdObj);
SRSCSDProcMode	SRS_CSD_GetProcMode(SRSCSDObj csdObj);
SRSCSDOutMode	SRS_CSD_GetOutMode(SRSCSDObj csdObj);
SRSBool			SRS_CSD_GetPhantomEnable(SRSCSDObj csdObj);
SRSBool			SRS_CSD_GetCenterFullBandEnable(SRSCSDObj csdObj);


//Version query:
unsigned char	SRS_CSD_GetTechVersion(SRSVersion which);
unsigned char	SRS_CSD_GetLibVersion(SRSVersion which);

#ifdef __cplusplus
}
#endif /*__cplusplus*/




#endif /*__SRS_CSDECODER_API_H__*/

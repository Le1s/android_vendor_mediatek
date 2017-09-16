/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_srs3d APIs
 *
 *	Author: Zesen Zhuang
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_srs3d/std_fxp/include/srs_srs3d_api.h#6 $
 *  $Author: oscarh $
 *  $Date: 2010/11/16 $
 *	
********************************************************************************/

#ifndef __SRS_SRS3D_API_H__
#define __SRS_SRS3D_API_H__

#include "srs_srs3d_def.h"
#include "srs_fxp.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:
int				SRS_SRS3D_GetObjSize();
SRSResult		SRS_SRS3D_CreateObj(SRSSrs3DObj *pObj, void* pBuf, SRS_Allocator alloc,void* pContext);

SRSResult		SRS_SRS3D_InitObj8k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj11k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj16k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj22k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj24k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj32k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj44k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj48k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj88k(SRSSrs3DObj srs3dObj);
SRSResult		SRS_SRS3D_InitObj96k(SRSSrs3DObj srs3dObj);

SRSResult		SRS_SRS3D_Process(SRSSrs3DObj srs3dObj, SRSStereoCh *input, SRSStereoCh *output, int blockSize, void *ws);

//////////////SRS_SRS3D_Set//////////////////////////////////////////////////////////

void			SRS_SRS3D_SetControlDefaults(SRSSrs3DObj srs3dObj);
void			SRS_SRS3D_SetEnable(SRSSrs3DObj srs3dObj, SRSBool enable);
SRSResult		SRS_SRS3D_SetInputGain(SRSSrs3DObj srs3dObj, SRSInt16 gain);	//I16.1 0.0-1.0
SRSResult		SRS_SRS3D_SetOutputGain(SRSSrs3DObj srs3dObj, SRSInt16 gain);	//I16.1 0.0-1.0
SRSResult		SRS_SRS3D_SetBypassGain(SRSSrs3DObj srs3dObj, SRSInt16 gain);	//I16.1 0.0-1.0
SRSResult		SRS_SRS3D_SetSpaceLevel(SRSSrs3DObj srs3dObj, SRSInt16 level);	//I16.1 0.0-1.0
SRSResult		SRS_SRS3D_SetCenterLevel(SRSSrs3DObj srs3dObj, SRSInt16 level);	//I16.1 0.0-1.0
void			SRS_SRS3D_SetHeadphoneEnable(SRSSrs3DObj srs3dObj, SRSBool enable);
void			SRS_SRS3D_SetHighBitRateEnable(SRSSrs3DObj srs3dObj, SRSBool enable);
SRSResult		SRS_SRS3D_SetMode(SRSSrs3DObj srs3dObj, SRSSrs3DMode mode);

//////////////SRS_SRS3D_Get//////////////////////////////////////////////////////////

SRSBool			SRS_SRS3D_GetEnable(SRSSrs3DObj srs3dObj);
SRSInt16		SRS_SRS3D_GetInputGain(SRSSrs3DObj srs3dObj);
SRSInt16		SRS_SRS3D_GetOutputGain(SRSSrs3DObj srs3dObj);
SRSInt16		SRS_SRS3D_GetBypassGain(SRSSrs3DObj srs3dObj);
SRSInt16		SRS_SRS3D_GetSpaceLevel(SRSSrs3DObj srs3dObj);
SRSInt16		SRS_SRS3D_GetCenterLevel(SRSSrs3DObj srs3dObj);
SRSBool			SRS_SRS3D_GetHeadphoneEnable(SRSSrs3DObj srs3dObj);
SRSBool			SRS_SRS3D_GetHighBitRateEnable(SRSSrs3DObj srs3dObj);
SRSSrs3DMode	SRS_SRS3D_GetMode(SRSSrs3DObj srs3dObj);


unsigned char	SRS_SRS3D_GetTechVersion(SRSVersion which);
unsigned char	SRS_SRS3D_GetLibVersion(SRSVersion which);

#ifdef __cplusplus
}
#endif /*__cplusplus*/


#endif /*__SRS_SRS3D_API_H__*/

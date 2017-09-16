/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Exposes all srs_fft APIs
 *
 *	Author: Zesen Zhuang
 *
 *  RCS keywords:
 *	$Id: //srstech/srs_fft/std_fxp/include/srs_fft_api.h#6 $
 *  $Author: zesenz $
 *  $Date: 2011/01/06 $
 *	
********************************************************************************/

#ifndef __SRS_FFT_API_H__
#define __SRS_FFT_API_H__

#include "srs_fft_def.h"

#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

//API declaration here:

int	SRS_Fft_GetObjSize(void);

/* initialization API */
SRSResult SRS_Fft_CreateObj(SRSFftObj* pObj,void* pBuf,SRS_Allocator alloc, void* pContext, const SRSFftCreateData *pAttr);

/* APIs for FFT/IFFT computations */
void SRS_Rfft_32c16(SRSFftObj fftObj, SRSInt32 *in, SRSInt32 *out);
void SRS_InvRfft_32c16(SRSFftObj fftObj, SRSInt32 *in, SRSInt32 *out);
void SRS_Rfft_16c16(SRSFftObj fftObj, SRSInt16 *in, SRSInt16 *out);
void SRS_InvRfft_16c16(SRSFftObj fftObj, SRSInt16 *in, SRSInt16 *out);
void SRS_Cfft_32c16(SRSFftObj fftObj, SRSInt32 *in, SRSInt32 *out);
void SRS_InvCfft_32c16(SRSFftObj fftObj, SRSInt32 *in, SRSInt32 *out);

unsigned char SRS_FFT_GetTechVersion(SRSVersion which);
unsigned char SRS_FFT_GetLibVersion(SRSVersion which);


#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif /*__SRS_FFT_API_H__*/

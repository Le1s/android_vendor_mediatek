/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS TBHDX filter design APIs
 *
 *  Authour: Oscar Huang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author: oscarh $
 *  $Date: 2012/05/10$
 *	
********************************************************************************/
#ifndef __SRS_TBHDX_DESIGN_API_H__
#define __SRS_TBHDX_DESIGN_API_H__

#include "srs_tbhdx_design_def.h"
#include "srs_typedefs.h"

#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/


/******************************************************************************************
 * Design floating point filters for TBHDX
 * Parameters:
 *	spkSize: [IN] The speaker size in Hz, must be positive
 *  tGain: [IN] Temporal gain, a.k.a Bass Control, valid range [0.0, 1.0]
 *  hpRatio: [IN] High pass ratio, valid range [0, 1.0]
 *  extBass: [IN] Extended bass control, valid range [0, 1.0]
 *  samplingRate: [IN] The sampling rate in Hz
 *  coefs: [OUT] The buffer for the designed filter coefficients. The size of the buffer must be
 *               at least SRS_TBHDX_FLOAT_COEFFICIENT_ARRAY_LEN elements. Filter coefficients are 
 *               arranged in the following pattern:
	union
	{
		struct
		{
			float					LowPassFilterCoef[8];			//{b0,b1,a1;  b0,b1,b2,a1,a2}, the first 3 elements are coefficients for 1st order LPF, the rest are for 2nd order LPF
			float					HighPassFilterCoef[8];			//{b0,b1,a1;  b0,b1,b2,a1,a2}, the first 3 elements are coefficients for 1st order HPF, the rest are for 2nd order HPF
			float					ShelvingFilterCoef[5];			//{b0,b1,b2,a1,a2}
			float					GainFilterCoef[5];				//{b0,b1,b2,a1,a2}
			float					HPShelvingFilterCoef[3];		//{b0,b1,a1}

			float					SpkSize;						//safe to cast it to int
			float					tGain;
			float					hpRatio;
			float					extBass;
		} Struct;
		float	Array[SRS_TBHDX_FLOAT_COEFFICIENT_ARRAY_LEN];
	} SRSTBHDXCustomFilters;
*******************************************************************************************/
SRSResult  SRS_TBHDX_FilterDesignFloat(int spkSize, float tGain, float hpRatio, float extBass, int sampleRate, float *coefs);




/******************************************************************************************
 * Design fixed-point filters for TBHDX
 * Parameters:
 *	spkSize: [IN] The speaker size in Hz, must be positive
 *  tGain: [IN] Temporal gain, a.k.a Bass Control, valid range [0.0, 1.0]
 *  hpRatio: [IN] High pass ratio, valid range [0, 1.0]
 *  extBass: [IN] Extended bass control, valid range [0, 1.0]
 *  samplingRate: [IN] The sampling rate in Hz
 *  coefs: [OUT] The buffer for the designed filter coefficients. The size of the buffer must be
 *               at least SRS_TBHDX_FXP_COEFFICIENT_ARRAY_LEN elements. Filter coefficients are 
 *               arranged in the following pattern:
	union
	{
		struct
		{
			fxpType					LowPassFilterCoef[10];		//{iwl, b0,b1,a1;  iwl, b0,b1,b2,a1,a2}the first 4 elements are coefficients for 1st order LPF, the rest are for 2nd order LPF
			fxpType					HighPassFilterCoef[10];		//{iwl, b0,b1,a1;  iwl, b0,b1,b2,a1,a2}the first 4 elements are coefficients for 1st order HPF, the rest are for 2nd order HPF
			fxpType					ShelvingFilterCoef[6];		//{iwl, b0,b1,b2,a1,a2}	
			fxpType					GainFilterCoef[6];			//{iwl, b0,b1,b2,a1,a2}  
			fxpType					HPShelvingFilterCoef[4];	//{iwl, b0,b1,a1}  

			fxpType					SpkSize;					//safe to cast it to int
			fxpType					tGain;						//iwl: 2
			fxpType					hpRatio;					//iwl: 1
			fxpType					extBass;					//iwl: 1
		} Struct;
		fxpType	Array[SRS_TBHDX_FXP_COEFFICIENT_ARRAY_LEN];
	} SRSTBHDXCustomFilters;
 *
 *  ws:  [IN] workspace, whose size must be at least SRS_TBHDX_DESIGN_WORKSPACE_SIZE bytes
*******************************************************************************************/
SRSResult  SRS_TBHDX_FilterDesignFxp32(int spkSize, float tGain, float hpRatio, float extBass, int sampleRate, SRSInt32 *coefs, void *ws);
SRSResult  SRS_TBHDX_FilterDesignFxp24(int spkSize, float tGain, float hpRatio, float extBass, int sampleRate, SRSInt24 *coefs, void *ws);
SRSResult  SRS_TBHDX_FilterDesignFxp16(int spkSize, float tGain, float hpRatio, float extBass, int sampleRate, SRSInt16 *coefs, void *ws);


#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif //__SRS_TBHDX_DESIGN_API_H__

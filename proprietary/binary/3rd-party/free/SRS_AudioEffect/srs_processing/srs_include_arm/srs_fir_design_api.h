/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS IIR filter design APIs
 *
 *  Authour: Zesen Zhuang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/
#ifndef __SRS_FIR_DESIGN_API_H__
#define __SRS_FIR_DESIGN_API_H__

#include "srs_typedefs.h"
#include "srs_fir_design_def.h"

#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

/*************************************************************************************************
 * Scale floating point FIR filters using time-domain 1-norm. Scaling an FIR filter minimizing the
 * oveflow chances in the fixed-point FIR filtering code.This step should be done prio to quantizing
 * the FIR into fixed-point.
 *
 * Parameters:
 *		coefs:	[IN/OUT] The filter coefficients. The filter coefficients must be
 *                    in the array in the following pattern:
 *						{b0, b1, b2, ...,bn, gain}
 *
 *		order:	[IN] Filter order
 *
**************************************************************************************************/
void	SRS_ScaleFIR(float *coefs, int order);


/*************************************************************************************************
 *  Quantize a floating point FIR to fxp32, fxp24 or fxp16.
 *
 *
 * Parameters:
 *		fir:	[IN] The floating point FIR coefficients array
 *		order:	[IN] The order of the FIR
 *		fxpFIR:	[OUT] The quantized fixed-point coefficients array, whose length is SRS_FIR_FXP_COEFFICIENT_ARRAY_LEN(order)
					  Allocate sufficient memory for it prior to calling the functions.
 * Remarks:
 *		The floating point FIR coefficients array is in the pattern: {b0, b1, b2, ...bn, gain}
 *		The fixed-point FIR coefficients array is in the pattern: {iwl, b0, b1, b2, ...bn, gain_iwl, gain}
 *
**************************************************************************************************/
void	SRS_QuantizeFIR_Fxp32(const float *fir, int order,  SRSInt32  *fxpFIR); //quantize to 32-bit fxp
void	SRS_QuantizeFIR_Fxp24(const float *fir, int order,  SRSInt24  *fxpFIR);	//quantize to 24-bit fxp
void	SRS_QuantizeFIR_Fxp16(const float *fir, int order,  SRSInt16  *fxpFIR);	//quantize to 16-bit fxp


/*************************************************************************************************
 *  Converts fixed-point FIR to floating point version
 *
 *
 * Parameters:
 *		fxpFIR:	[IN] The fixed-point FIR coefficients array
 *		order:	[IN] The order of the FIR
 *		fir:	[OUT] The converted floating point coefficients array, whose length is SRS_FIR_FLOAT_COEFFICIENT_ARRAY_LEN(order)
					  Allocate sufficient memory for it prior to calling the functions.
 *
**************************************************************************************************/
void	SRS_Fxp32FIRToFloat(const SRSInt32 *fxpFIR, int order, float *fir);	//32-bit fxp to floating point
void	SRS_Fxp24FIRToFloat(const SRSInt24 *fxpFIR, int order, float *fir);	//24-bit fxp to floating point
void	SRS_Fxp16FIRToFloat(const SRSInt16 *fxpFIR, int order, float *fir);	//16-bit fxp to floating point

#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif //__SRS_FIR_DESIGN_API_H__

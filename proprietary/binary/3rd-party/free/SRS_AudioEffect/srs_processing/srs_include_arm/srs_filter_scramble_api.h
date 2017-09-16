/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2011 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  Functions for scrambling/unscrambling IIR and FIR coefficients
 *
 *  Authour: Oscar Huang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/
#ifndef __SRS_FILTER_SCRAMBLE_API_H__
#define __SRS_FILTER_SCRAMBLE_API_H__

#include "srs_typedefs.h"


#ifdef __cplusplus
extern "C"{
#endif /*__cplusplus*/

/*************************************************************************************************
 *  Scrambles/unscrambles SRSFoat32 type SOS cascaded IIR coefficients.
 *
 * Parameters:
 *		orgCoef:[IN] The bit-pattern of the floating point IIR coefficients.
 *		order:	[IN] The order of the IIR
 *		scrambled:	[OUT] The bit-pattern of the scrambled/unscambled filter coefficients.The length
 *                        is SRS_IIR_FLOAT_COEFFICIENT_ARRAY_LEN(order)
 *
 * Remarks:
 *		The function supports in place operation, that is, scrambled can be set to orgCoef.The format
 *		of the filter coefficients is assumed identical to the one in function SRS_IirFilterDesignFloat.
 *
**************************************************************************************************/
void	SRS_Float32IIRScramble(SRSInt32 *orgCoef, int order, SRSInt32 *scrambled);



/*************************************************************************************************
 *  Scrambles/unscrambles SRSFoat32 type FIR coefficients.
 *
 * Parameters:
 *		orgCoef:[IN] The bit-pattern of the floating point FIR coefficients.
 *		order:	[IN] The order of the FIR. order=taps-1
 *		scrambled:	[OUT] The bit-pattern of the scrambled/unscambled filter coefficients.The length
 *                        is order+2
 *
 * Remarks:
 *		The function supports in place operation, that is, scrambled can be set to orgCoef.
 *
**************************************************************************************************/
void	SRS_Float32FIRScramble(SRSInt32 *orgCoef, int order, SRSInt32 *scrambled);


/*************************************************************************************************
 *  Scrambles/unscrambles std_fxp type SOS cascaded IIR coefficients.
 *
 * Parameters:
 *		orgCoef:[IN] The fixed-point IIR coefficients.
 *		order:	[IN] The order of the IIR
 *		scrambled:	[OUT] The scrambled/unscambled filter coefficients.The length
 *                        is SRS_IIR_FXP_COEFFICIENT_ARRAY_LEN(order)
 *
 * Remarks:
 *		The function supports in place operation, that is, scrambled can be set to orgCoef.The format
 *		of the filter coefficients is assumed identical to the one in function SRS_IirFilterDesignFxp32.
 *
**************************************************************************************************/
void	SRS_StdFxpIIRScramble(SRSInt32 *orgCoef, int order, SRSInt32 *scrambled);


/*************************************************************************************************
 *  Scrambles/unscrambles std_fxp type FIR coefficients.
 *
 * Parameters:
 *		orgCoef:[IN] The fixed-point FIR coefficients.
 *		order:	[IN] The order of the FIR. order=taps-1
 *		scrambled:	[OUT] The bit-pattern of the scrambled/unscambled filter coefficients. The length
 *                        is order+4
 *
 * Remarks:
 *		The function supports in place operation, that is, scrambled can be set to orgCoef.
 *
**************************************************************************************************/
void	SRS_StdFxpFIRScramble(SRSInt32 *orgCoef, int order, SRSInt32 *scrambled);


/*************************************************************************************************
 *  Scrambles/unscrambles 24-bit fxp type SOS cascaded IIR coefficients.
 *
 * Parameters:
 *		orgCoef:[IN] The fixed-point IIR coefficients.
 *		order:	[IN] The order of the IIR
 *		scrambled:	[OUT] The scrambled/unscambled filter coefficients. The length
 *                        is SRS_IIR_FXP_COEFFICIENT_ARRAY_LEN(order)
 *
 * Remarks:
 *		The function supports in place operation, that is, scrambled can be set to orgCoef.The format
 *		of the filter coefficients is assumed identical to the one in function SRS_IirFilterDesignFxp24.
 *
**************************************************************************************************/
void	SRS_Fxp24IIRScramble(SRSInt24 *orgCoef, int order, SRSInt24 *scrambled);


/*************************************************************************************************
 *  Scrambles/unscrambles std_fxp type FIR coefficients.
 *
 * Parameters:
 *		orgCoef:[IN] The fixed-point FIR coefficients.
 *		order:	[IN] The order of the FIR. order=taps-1
 *		scrambled:	[OUT] The bit-pattern of the scrambled/unscambled filter coefficients. The length
 *                        is order+4
 *
 * Remarks:
 *		The function supports in place operation, that is, scrambled can be set to orgCoef.
 *
**************************************************************************************************/
void	SRS_Fxp24FIRScramble(SRSInt24 *orgCoef, int order, SRSInt24 *scrambled);


/*************************************************************************************************
 *  Scrambles/unscrambles 16-bit fxp type SOS cascaded IIR coefficients.
 *
 * Parameters:
 *		orgCoef:[IN] The fixed-point IIR coefficients.
 *		order:	[IN] The order of the IIR
 *		scrambled:	[OUT] The scrambled/unscambled filter coefficients. The length
 *                        is SRS_IIR_FXP_COEFFICIENT_ARRAY_LEN(order)
 *
 * Remarks:
 *		The function supports in place operation, that is, scrambled can be set to orgCoef.The format
 *		of the filter coefficients is assumed identical to the one in function SRS_IirFilterDesignFxp16.
 *
**************************************************************************************************/
void	SRS_Fxp16IIRScramble(SRSInt16 *orgCoef, int order, SRSInt16 *scrambled);


/*************************************************************************************************
 *  Scrambles/unscrambles std_fxp type FIR coefficients.
 *
 * Parameters:
 *		orgCoef:[IN] The fixed-point FIR coefficients.
 *		order:	[IN] The order of the FIR. order=taps-1
 *		scrambled:	[OUT] The bit-pattern of the scrambled/unscambled filter coefficients. The length
 *                        is order+4
 *
 * Remarks:
 *		The function supports in place operation, that is, scrambled can be set to orgCoef.
 *
**************************************************************************************************/
void	SRS_Fxp16FIRScramble(SRSInt16 *orgCoef, int order, SRSInt16 *scrambled);


#ifdef __cplusplus
}
#endif /*__cplusplus*/

#endif //__SRS_FILTER_SCRAMBLE_API_H__

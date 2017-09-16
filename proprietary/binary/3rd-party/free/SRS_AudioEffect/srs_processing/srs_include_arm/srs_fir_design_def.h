/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2011 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS FIR filter design types, constants
 *
 *  Authour: Oscar Huang
 *
 *  RCS keywords:
 *	$Id$
 *  $Author$
 *  $Date$
 *	
********************************************************************************/
#ifndef __SRS_FIR_DESIGN_DEF_H__
#define __SRS_FIR_DESIGN_DEF_H__

#include "srs_filter_design_def.h"



#define SRS_FIR_FLOAT_COEFFICIENT_ARRAY_LEN(order)		((order)+1+1)	//in float type elements
#define SRS_FIR_FXP_COEFFICIENT_ARRAY_LEN(order)		((order)+1+1+2)	//in SRSInt32, SRSInt24 or SRSInt16 elements



#endif //__SRS_FIR_DESIGN_DEF_H__

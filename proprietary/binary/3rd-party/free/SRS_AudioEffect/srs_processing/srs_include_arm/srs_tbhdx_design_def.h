/********************************************************************************
 *	SRS Labs CONFIDENTIAL
 *	@Copyright 2010 by SRS Labs.
 *	All rights reserved.
 *
 *  Description:
 *  SRS TBHDX filter design types, constants
 *
 *  Authour: Oscar Huang
 *
 *  RCS keywords:
 *	$Id $
 *  $Author: oscarh $
 *  $Date: 2012/05/10 $
 *	
********************************************************************************/
#ifndef __SRS_TBHDX_DESIGN_DEF_H__
#define __SRS_TBHDX_DESIGN_DEF_H__

#include "srs_parametriceq_design_def.h"

#define SRS_TBHDX_FLOAT_COEFFICIENT_ARRAY_LEN	33		//in elements
#define SRS_TBHDX_FXP_COEFFICIENT_ARRAY_LEN		40		//in elements

#define SRS_TBHDX_DESIGN_WORKSPACE_SIZE		(SRS_TBHDX_FLOAT_COEFFICIENT_ARRAY_LEN*sizeof(float) + 8) //in bytes


#endif //__SRS_TBHDX_DESIGN_DEF_H__
#define __CLC_SCALAR_GENTYPE float

#define __CLC_GENTYPE float
#define __CLC_SCALAR
#define __CLC_GENTYPE_CORESP_INT int
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_SCALAR
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE float2
#define __CLC_GENTYPE_CORESP_INT int2
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE float3
#define __CLC_GENTYPE_CORESP_INT int3
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE float4
#define __CLC_GENTYPE_CORESP_INT int4
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE float8
#define __CLC_GENTYPE_CORESP_INT int8
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE float16
#define __CLC_GENTYPE_CORESP_INT int16
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#undef __CLC_SCALAR_GENTYPE

#ifdef cl_khr_fp64
#pragma OPENCL EXTENSION cl_khr_fp64 : enable
#define __CLC_SCALAR_GENTYPE double

#define __CLC_SCALAR
#define __CLC_GENTYPE double
#define __CLC_GENTYPE_CORESP_INT long
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_SCALAR
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE double2
#define __CLC_GENTYPE_CORESP_INT long2
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE double3
#define __CLC_GENTYPE_CORESP_INT long3
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE double4
#define __CLC_GENTYPE_CORESP_INT long4
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE double8
#define __CLC_GENTYPE_CORESP_INT long8
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#define __CLC_GENTYPE double16
#define __CLC_GENTYPE_CORESP_INT long16
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_GENTYPE_CORESP_INT

#undef __CLC_SCALAR_GENTYPE
#endif

#undef __CLC_BODY

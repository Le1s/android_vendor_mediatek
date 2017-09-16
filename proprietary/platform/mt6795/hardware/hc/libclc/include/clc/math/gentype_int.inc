#define __CLC_GENTYPEF
#define __CLC_SCALAR_GENTYPE float

#define __CLC_GENTYPE float
#define __CLC_INTTYPE int
#define __CLC_UINTTYPE uint
#define __CLC_SCALAR
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE
#undef __CLC_SCALAR

#define __CLC_GENTYPE float2
#define __CLC_INTTYPE int2
#define __CLC_UINTTYPE uint2
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#define __CLC_GENTYPE float3
#define __CLC_INTTYPE int3
#define __CLC_UINTTYPE uint3
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#define __CLC_GENTYPE float4
#define __CLC_INTTYPE int4
#define __CLC_UINTTYPE uint4
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#define __CLC_GENTYPE float8
#define __CLC_INTTYPE int8
#define __CLC_UINTTYPE uint8
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#define __CLC_GENTYPE float16
#define __CLC_INTTYPE int16
#define __CLC_UINTTYPE uint16
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#undef __CLC_SCALAR_GENTYPE
#undef __CLC_GENTYPEF

#ifdef cl_khr_fp64
#define __CLC_GENTYPED
#define __CLC_SCALAR_GENTYPE double

#define __CLC_SCALAR
#define __CLC_GENTYPE double
#define __CLC_INTTYPE int
#define __CLC_UINTTYPE uint
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE
#undef __CLC_SCALAR

#define __CLC_GENTYPE double2
#define __CLC_INTTYPE int2
#define __CLC_UINTTYPE uint2
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#define __CLC_GENTYPE double3
#define __CLC_INTTYPE int3
#define __CLC_UINTTYPE uint3
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#define __CLC_GENTYPE double4
#define __CLC_INTTYPE int4
#define __CLC_UINTTYPE uint4
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#define __CLC_GENTYPE double8
#define __CLC_INTTYPE int8
#define __CLC_UINTTYPE uint8
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#define __CLC_GENTYPE double16
#define __CLC_INTTYPE int16
#define __CLC_UINTTYPE uint16
#include __CLC_BODY
#undef __CLC_GENTYPE
#undef __CLC_INTTYPE
#undef __CLC_UINTTYPE

#undef __CLC_SCALAR_GENTYPE
#undef __CLC_GENTYPED
#endif

#undef __CLC_BODY

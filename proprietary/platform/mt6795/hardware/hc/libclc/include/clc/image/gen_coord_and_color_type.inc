#define __COLOR_TYPE    float4
#define __COLOR_SCALAR  float
#define __CLC_COORDTYPE float4
#include __CHARM_READ_IMG_BODY
#undef  __CLC_COORDTYPE 
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 

#define __COLOR_TYPE    float4
#define __COLOR_SCALAR  float
#define __CLC_COORDTYPE   int4
#include __CHARM_READ_IMG_BODY
#undef  __CLC_COORDTYPE 
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 

#define __COLOR_TYPE      int4
#define __COLOR_SCALAR    int
#define __CLC_COORDTYPE float4
#include __CHARM_READ_IMG_BODY
#undef  __CLC_COORDTYPE 
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 

#define __COLOR_TYPE      int4
#define __COLOR_SCALAR    int
#define __CLC_COORDTYPE   int4
#include __CHARM_READ_IMG_BODY
#undef  __CLC_COORDTYPE 
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 

#define __COLOR_TYPE     uint4
#define __COLOR_SCALAR   uint
#define __CLC_COORDTYPE float4
#include __CHARM_READ_IMG_BODY
#undef  __CLC_COORDTYPE 
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 

#define __COLOR_TYPE     uint4
#define __COLOR_SCALAR   uint
#define __CLC_COORDTYPE   int4
#include __CHARM_READ_IMG_BODY
#undef  __CLC_COORDTYPE 
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 

#undef __CHARM_READ_IMG_BODY

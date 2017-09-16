#define __CLC_COORDTYPE float4
#include __CHARM_READ_IMG_BODY
#undef  __CLC_COORDTYPE 

#define __CLC_COORDTYPE int4
#include __CHARM_READ_IMG_BODY
#undef  __CLC_COORDTYPE 

#undef __CHARM_READ_IMG_BODY

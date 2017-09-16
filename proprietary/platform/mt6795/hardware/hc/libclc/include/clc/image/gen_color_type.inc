#define __COLOR_TYPE    float4
#include __GET_IMG_ELE_BODY
#undef  __COLOR_TYPE 

#define __COLOR_TYPE    int4
#include __GET_IMG_ELE_BODY
#undef  __COLOR_TYPE 

#define __COLOR_TYPE    uint4
#include __GET_IMG_ELE_BODY
#undef  __COLOR_TYPE 

#undef __GET_IMG_ELE_BODY

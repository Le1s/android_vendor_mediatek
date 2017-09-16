#define __COLOR_TYPE    float4
#define __COLOR_SCALAR  float
#include __FILTER_LINEAR_BODY
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 

#define __COLOR_TYPE    int4
#define __COLOR_SCALAR  int
#include __FILTER_LINEAR_BODY
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 

#define __COLOR_TYPE    uint4
#define __COLOR_SCALAR  uint
#include __FILTER_LINEAR_BODY
#undef  __COLOR_TYPE 
#undef  __COLOR_SCALAR 


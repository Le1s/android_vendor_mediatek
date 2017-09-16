#undef half_rsqrt
#define half_rsqrt __clc_half_rsqrt

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_half_rsqrt

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION

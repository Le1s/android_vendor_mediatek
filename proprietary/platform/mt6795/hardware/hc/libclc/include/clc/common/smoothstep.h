#undef smoothstep
#define smoothstep __clc_smoothstep

#define __CLC_BODY <clc/common/ternary_ssv_decl.inc>
#define __CLC_FUNCTION __clc_smoothstep

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


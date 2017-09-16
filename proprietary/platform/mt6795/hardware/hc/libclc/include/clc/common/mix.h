#undef mix
#define mix __clc_mix

#define __CLC_BODY <clc/common/ternary_vvs_decl.inc>
#define __CLC_FUNCTION __clc_mix

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


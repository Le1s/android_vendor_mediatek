#undef acos
#define acos __clc_acos

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_acos

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


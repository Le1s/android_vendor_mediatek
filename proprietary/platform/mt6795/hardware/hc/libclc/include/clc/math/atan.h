#undef atan
#define atan __clc_atan

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_atan

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


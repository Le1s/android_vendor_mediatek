#undef expm1
#define expm1 __clc_expm1

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_expm1

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


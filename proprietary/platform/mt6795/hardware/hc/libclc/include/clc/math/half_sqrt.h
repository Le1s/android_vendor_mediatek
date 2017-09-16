#undef half_sqrt
#define half_sqrt __clc_half_sqrt

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_half_sqrt

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


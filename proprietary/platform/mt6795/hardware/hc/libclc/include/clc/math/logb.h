#undef logb
#define logb __clc_logb

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_logb

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


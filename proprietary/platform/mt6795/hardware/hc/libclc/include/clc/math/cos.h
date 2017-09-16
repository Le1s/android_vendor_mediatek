#undef cos
#define cos __clc_cos

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_cos

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


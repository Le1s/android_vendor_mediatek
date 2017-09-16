#undef exp
#define exp __clc_exp

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_exp

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


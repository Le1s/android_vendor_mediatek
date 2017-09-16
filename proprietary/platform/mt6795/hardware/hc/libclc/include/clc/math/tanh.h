#undef tanh
#define tanh __clc_tanh

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_tanh

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


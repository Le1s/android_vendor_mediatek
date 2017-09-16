#undef acosh
#define acosh __clc_acosh

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_acosh

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


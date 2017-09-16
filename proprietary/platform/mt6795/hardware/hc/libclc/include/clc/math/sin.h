#undef sin
#define sin __clc_sin

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_sin

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


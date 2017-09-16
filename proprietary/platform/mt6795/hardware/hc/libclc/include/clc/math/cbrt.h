#undef cbrt
#define cbrt __clc_cbrt

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_cbrt

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


#undef trunc
#define trunc __clc_trunc

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_trunc

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


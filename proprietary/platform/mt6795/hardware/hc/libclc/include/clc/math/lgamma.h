#undef lgamma
#define lgamma __clc_lgamma

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_lgamma

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


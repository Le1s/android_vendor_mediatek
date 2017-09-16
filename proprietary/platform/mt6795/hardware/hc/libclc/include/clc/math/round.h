#undef round
#define round __clc_round

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_round

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


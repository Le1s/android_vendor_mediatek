#undef half_recip
#define half_recip __clc_half_recip

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_half_recip

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


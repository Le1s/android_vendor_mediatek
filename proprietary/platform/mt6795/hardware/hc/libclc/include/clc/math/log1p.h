#undef log1p
#define log1p __clc_log1p

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_log1p

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


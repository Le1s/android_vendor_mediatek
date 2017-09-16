#undef log10
#define log10 __clc_log10

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_log10

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


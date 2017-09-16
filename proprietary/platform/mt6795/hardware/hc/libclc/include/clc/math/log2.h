#undef log2
#define log2 __clc_log2

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_log2

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


#undef native_sqrt
#define native_sqrt __clc_native_sqrt

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_native_sqrt

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION

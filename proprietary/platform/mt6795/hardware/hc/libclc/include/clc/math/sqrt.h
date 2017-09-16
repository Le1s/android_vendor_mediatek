#undef sqrt
#define sqrt __clc_sqrt

//#define __CLC_FUNCTION __clc_sqrt
//#define __CLC_INTRINSIC "llvm.sqrt"
//#include <clc/math/unary_intrin.inc>

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_sqrt

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION

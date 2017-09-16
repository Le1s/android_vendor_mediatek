#undef floor
#define floor __clc_floor

//#define __CLC_FUNCTION __clc_floor
//#define __CLC_INTRINSIC "llvm.floor"
//#include <clc/math/unary_intrin.inc>

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_floor

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION

#undef fabs
#define fabs __clc_fabs

//#define __CLC_FUNCTION __clc_fabs
//#define __CLC_INTRINSIC "llvm.fabs"
//#include <clc/math/unary_intrin.inc>

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_fabs

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION

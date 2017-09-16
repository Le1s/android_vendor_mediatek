#undef rint
#define rint __clc_rint

//#define __CLC_FUNCTION __clc_rint
//#define __CLC_INTRINSIC "llvm.rint"
//#include <clc/math/unary_intrin.inc>

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_rint

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION

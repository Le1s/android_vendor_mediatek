#undef asinpi
#define asinpi __clc_asinpi

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_asinpi

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


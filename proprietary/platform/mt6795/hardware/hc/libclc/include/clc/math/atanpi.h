#undef atanpi
#define atanpi __clc_atanpi

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_atanpi

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


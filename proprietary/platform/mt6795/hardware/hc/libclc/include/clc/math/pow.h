#undef pow
#define pow __clc_pow

#define __CLC_BODY <clc/math/binary_decl_gentype.inc>
#define __CLC_FUNCTION __clc_pow

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


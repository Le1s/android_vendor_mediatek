#undef half_divide
#define half_divide __clc_half_divide

#define __CLC_BODY <clc/math/binary_decl_gentype.inc>
#define __CLC_FUNCTION __clc_half_divide

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


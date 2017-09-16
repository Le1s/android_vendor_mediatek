#undef atan2
#define atan2 __clc_atan2

#define __CLC_BODY <clc/math/binary_decl_gentype.inc>
#define __CLC_FUNCTION __clc_atan2

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


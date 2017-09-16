#undef atan2pi
#define atan2pi __clc_atan2pi

#define __CLC_BODY <clc/math/binary_decl.inc>
#define __CLC_FUNCTION __clc_atan2pi

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


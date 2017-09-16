#undef sinh
#define sinh __clc_sinh

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_sinh

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


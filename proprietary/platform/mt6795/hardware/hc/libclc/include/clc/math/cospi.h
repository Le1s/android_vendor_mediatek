#undef cospi
#define cospi __clc_cospi

#define __CLC_BODY <clc/math/unary_decl.inc>
#define __CLC_FUNCTION __clc_cospi

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


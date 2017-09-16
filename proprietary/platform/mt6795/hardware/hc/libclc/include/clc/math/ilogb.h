#undef ilogb
#define ilogb __clc_ilogb

#define __CLC_BODY <clc/math/unary_decl.inc>

#define __CLC_RETTYPE __CLC_INTTYPE
#define __CLC_FUNCTION __clc_ilogb

#include <clc/math/gentype_int.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION
#undef __CLC_RETTYPE

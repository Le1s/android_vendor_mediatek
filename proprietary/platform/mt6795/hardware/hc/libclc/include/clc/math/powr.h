#undef powr
#define powr __clc_powr

#define __CLC_BODY <clc/math/binary_decl_gentype.inc>
#define __CLC_FUNCTION __clc_powr

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


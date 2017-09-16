#undef fdim
#define fdim __clc_fdim

#define __CLC_BODY <clc/math/binary_decl_gentype.inc>
#define __CLC_FUNCTION __clc_fdim

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


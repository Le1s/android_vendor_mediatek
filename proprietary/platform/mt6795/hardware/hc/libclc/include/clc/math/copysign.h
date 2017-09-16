#undef copysign
#define copysign __clc_copysign

#define __CLC_BODY <clc/math/binary_decl_gentype.inc>
#define __CLC_FUNCTION __clc_copysign

#include <clc/math/gentype.inc>

#undef __CLC_BODY
#undef __CLC_FUNCTION


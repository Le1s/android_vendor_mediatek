//#define bitselect(x, y, z) ((x) ^ ((z) & ((y) ^ (x))))

// integers
#define __CLC_BODY <clc/relational/bitselect.inc>
#include <clc/integer-gentype.inc>
#undef __CLC_BODY

// floats
#define BODY <clc/relational/bitselect.inc>
#include <clc/gentype.inc>
#undef BODY

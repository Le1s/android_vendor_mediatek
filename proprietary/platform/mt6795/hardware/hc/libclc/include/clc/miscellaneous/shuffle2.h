#define SHUFFLE2_DECL(PRIM_TYPE, MASK_TYPE, ARG_SIZE, RET_SIZE) \
  _CLC_OVERLOAD _CLC_DECL PRIM_TYPE##RET_SIZE shuffle2(PRIM_TYPE##ARG_SIZE x, \
                                                       PRIM_TYPE##ARG_SIZE y, \
                                                       MASK_TYPE##RET_SIZE mask);

#define SHUFFLE2_RET_SIZE(PRIM_TYPE, MASK_TYPE, ARG_SIZE) \
  SHUFFLE2_DECL(PRIM_TYPE, MASK_TYPE, ARG_SIZE, 2) \
  SHUFFLE2_DECL(PRIM_TYPE, MASK_TYPE, ARG_SIZE, 4) \
  SHUFFLE2_DECL(PRIM_TYPE, MASK_TYPE, ARG_SIZE, 8) \
  SHUFFLE2_DECL(PRIM_TYPE, MASK_TYPE, ARG_SIZE, 16)

#define SHUFFLE2_ARG_SIZE(PRIM_TYPE, MASK_TYPE) \
  SHUFFLE2_RET_SIZE(PRIM_TYPE, MASK_TYPE, 2) \
  SHUFFLE2_RET_SIZE(PRIM_TYPE, MASK_TYPE, 4) \
  SHUFFLE2_RET_SIZE(PRIM_TYPE, MASK_TYPE, 8) \
  SHUFFLE2_RET_SIZE(PRIM_TYPE, MASK_TYPE, 16)

#define SHUFFLE2_PRIM_TYPE() \
  SHUFFLE2_ARG_SIZE(char,  uchar)   \
  SHUFFLE2_ARG_SIZE(uchar, uchar)  \
  SHUFFLE2_ARG_SIZE(short,  ushort)  \
  SHUFFLE2_ARG_SIZE(ushort, ushort) \
  SHUFFLE2_ARG_SIZE(int,  uint)    \
  SHUFFLE2_ARG_SIZE(uint, uint)   \
  SHUFFLE2_ARG_SIZE(long,  ulong)   \
  SHUFFLE2_ARG_SIZE(ulong, ulong)  \
  SHUFFLE2_ARG_SIZE(float, uint)

SHUFFLE2_PRIM_TYPE()

#ifdef cl_khr_fp64
SHUFFLE2_ARG_SIZE(double, ulong)
#endif

// FIXME: argnument type "half" not support

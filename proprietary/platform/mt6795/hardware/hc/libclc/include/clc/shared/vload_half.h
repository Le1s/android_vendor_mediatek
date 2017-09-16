#define _CLC_VLOAD_HALF_DECL(ADDR_SPACE) \
  _CLC_OVERLOAD _CLC_DECL float vload_half(size_t offset, const ADDR_SPACE half *x);

#define _CLC_VLOAD_HALF() \
  _CLC_VLOAD_HALF_DECL( __private) \
  _CLC_VLOAD_HALF_DECL(__local) \
  _CLC_VLOAD_HALF_DECL(__constant) \
  _CLC_VLOAD_HALF_DECL(__global) \

_CLC_VLOAD_HALF()


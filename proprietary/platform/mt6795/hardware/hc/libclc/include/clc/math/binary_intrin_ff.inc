_CLC_OVERLOAD float __CLC_FUNCTION(float, float) __asm(__CLC_INTRINSIC ".f32");
_CLC_OVERLOAD float2 __CLC_FUNCTION(float2, float2) __asm(__CLC_INTRINSIC ".v2f32");
_CLC_OVERLOAD float3 __CLC_FUNCTION(float3, float3) __asm(__CLC_INTRINSIC ".v3f32");
_CLC_OVERLOAD float4 __CLC_FUNCTION(float4, float4) __asm(__CLC_INTRINSIC ".v4f32");
_CLC_OVERLOAD float8 __CLC_FUNCTION(float8, float8) __asm(__CLC_INTRINSIC ".v8f32");
_CLC_OVERLOAD float16 __CLC_FUNCTION(float16, float16) __asm(__CLC_INTRINSIC ".v16f32");

#undef __CLC_FUNCTION
#undef __CLC_INTRINSIC

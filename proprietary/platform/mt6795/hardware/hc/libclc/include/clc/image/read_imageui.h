 /* 6.12.14.2 Built-in Image Read Functions*/
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image2d_t image, sampler_t sampler, int2 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image2d_t image, sampler_t sampler, float2 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image3d_t image, sampler_t sampler, int4 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image3d_t image, sampler_t sampler, float4 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image2d_array_t image, sampler_t sampler, int4 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image2d_array_t image, sampler_t sampler, float4 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image1d_t image, sampler_t sampler, int coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image1d_t image, sampler_t sampler, float coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image1d_array_t image, sampler_t sampler, int2 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image1d_array_t image, sampler_t sampler, float2 coord);

/* 6.12.14.3 Built-in Image Sampler-less Read Functions */
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image2d_t image, int2 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image3d_t image, int4 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image2d_array_t image, int4 coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image1d_t image, int coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image1d_buffer_t image, int coord);
_CLC_OVERLOAD _CLC_DECL uint4 read_imageui(image1d_array_t image, int2 coord);


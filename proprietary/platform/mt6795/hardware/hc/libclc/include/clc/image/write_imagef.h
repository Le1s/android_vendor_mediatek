_CLC_OVERLOAD _CLC_DECL void write_imagef(image2d_t image,       int2 coord, float4 color);
_CLC_OVERLOAD _CLC_DECL void write_imagef(image2d_array_t image, int4 coord, float4 color);
_CLC_OVERLOAD _CLC_DECL void write_imagef(image1d_t image,        int coord, float4 color);
_CLC_OVERLOAD _CLC_DECL void write_imagef(image1d_buffer_t image, int coord, float4 color);
_CLC_OVERLOAD _CLC_DECL void write_imagef(image1d_array_t image, int2 coord, float4 color);

// Extension
_CLC_OVERLOAD _CLC_DECL void write_imagef(image3d_t  image, int4 coord, float4 color);

void charm_write_imagef(image_t *image, int4 coord, float4 *color);

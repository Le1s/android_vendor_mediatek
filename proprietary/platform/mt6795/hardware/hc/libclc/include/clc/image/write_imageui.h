_CLC_OVERLOAD _CLC_DECL void write_imageui(image2d_t image,       int2 coord, uint4 color);
_CLC_OVERLOAD _CLC_DECL void write_imageui(image2d_array_t image, int4 coord, uint4 color);
_CLC_OVERLOAD _CLC_DECL void write_imageui(image1d_t image,        int coord, uint4 color);
_CLC_OVERLOAD _CLC_DECL void write_imageui(image1d_buffer_t image, int coord, uint4 color);
_CLC_OVERLOAD _CLC_DECL void write_imageui(image1d_array_t image, int2 coord, uint4 color);

// Extension
_CLC_OVERLOAD _CLC_DECL void write_imageui(image3d_t image, int4 coord,  uint4 color);

void charm_write_imageui(image_t *image, int4 coord, uint4 *color);

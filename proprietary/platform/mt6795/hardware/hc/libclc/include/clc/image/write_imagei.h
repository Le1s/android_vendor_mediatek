_CLC_OVERLOAD _CLC_DECL void write_imagei(image2d_t image,       int2 coord, int4 color);
_CLC_OVERLOAD _CLC_DECL void write_imagei(image2d_array_t image, int4 coord, int4 color);
_CLC_OVERLOAD _CLC_DECL void write_imagei(image1d_t image,        int coord, int4 color);
_CLC_OVERLOAD _CLC_DECL void write_imagei(image1d_buffer_t image, int coord, int4 color);
_CLC_OVERLOAD _CLC_DECL void write_imagei(image1d_array_t image, int2 coord, int4 color);

// Extension
_CLC_OVERLOAD _CLC_DECL void write_imagei(image3d_t  image, int4 coord,   int4 color);

void charm_write_imagei(image_t *image, int4 coord, int4 *color);

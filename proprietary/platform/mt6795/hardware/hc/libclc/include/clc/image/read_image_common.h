_CLC_OVERLOAD void get_image_element(int x, int y, int z, image_t *image, float4 *color);
_CLC_OVERLOAD void get_image_element(int x, int y, int z, image_t *image, int4 *color);
_CLC_OVERLOAD void get_image_element(int x, int y, int z, image_t *image, uint4 *color);

_CLC_OVERLOAD void filter_linear_common(float4* color, image_t *image,
                                        int i0, int j0, int k0,
                                        int i1, int j1, int k1,
                                        float u, float v, float w, 
                                        int addr_mode);
_CLC_OVERLOAD void filter_linear_common(int4* color, image_t *image,
                                        int i0, int j0, int k0,
                                        int i1, int j1, int k1,
                                        float u, float v, float w, 
                                        int addr_mode);
_CLC_OVERLOAD void filter_linear_common(uint4* color, image_t *image,
                                        int i0, int j0, int k0,
                                        int i1, int j1, int k1,
                                        float u, float v, float w, 
                                        int addr_mode);

_CLC_OVERLOAD _CLC_DECL void charm_read_image(image_t *image, int sampler,
                                    float4 coord, float4 *color); 
_CLC_OVERLOAD _CLC_DECL void charm_read_image(image_t *image, int sampler,
                                    int4 coord, float4 *color); 
_CLC_OVERLOAD _CLC_DECL void charm_read_image(image_t *image, int sampler,
                                    float4 coord, int4 *color); 
_CLC_OVERLOAD _CLC_DECL void charm_read_image(image_t *image, int sampler,
                                    int4 coord, int4 *color); 
_CLC_OVERLOAD _CLC_DECL void charm_read_image(image_t *image, int sampler,
                                    float4 coord, uint4 *color); 
_CLC_OVERLOAD _CLC_DECL void charm_read_image(image_t *image, int sampler,
                                    int4 coord, uint4 *color); 

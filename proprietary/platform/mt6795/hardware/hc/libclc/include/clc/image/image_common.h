#include <clc/clc.h>

#define frac(x) (x-floor(x))
#define NOT_ARRAY 0
#define TWO_D_ARRAY  2
#define ONE_D_ARRAY  1

/* From OpenCL spec1.2 table 8.1 */
int addr_mode(int coord, int size, int addr_mode);

int get_num_channels(int image_channel_order);

int check_is_array(image_t *image);

bool is_out_of_bound(image_t *img, float u, float v, float w);

bool check_img_info(image_t *image, int *height, int *depth, int4 coord);

//void map_img_channel_order(int img_channel_order, float4 *color);

float half2float(ushort halfValue);

/*------  Used for get_image_width, 
                   get_image_channel_data_type, 
                   get_image_channel_order 
*/
#define __CLC_ALLIMAGETYPE image1d_t
#include __CLC_BODY
#undef  __CLC_ALLIMAGETYPE 
                   
#define __CLC_ALLIMAGETYPE image1d_buffer_t
#include __CLC_BODY
#undef  __CLC_ALLIMAGETYPE 

#define __CLC_ALLIMAGETYPE image2d_t
#include __CLC_BODY
#undef  __CLC_ALLIMAGETYPE 

#define __CLC_ALLIMAGETYPE image3d_t
#include __CLC_BODY
#undef  __CLC_ALLIMAGETYPE 

#define __CLC_ALLIMAGETYPE image1d_array_t
#include __CLC_BODY
#undef  __CLC_ALLIMAGETYPE 

#define __CLC_ALLIMAGETYPE image2d_array_t
#include __CLC_BODY
#undef  __CLC_ALLIMAGETYPE 

#undef __CLC_BODY

//for open()
#include    <sys/types.h>
#include    <sys/stat.h>
#include    <fcntl.h>

extern "C"
{
#include <stdio.h>
#include <stdarg.h>
}

//errno
#include    <errno.h>

//SMI kernel include
#include    <linux/ioctl.h>
 

#include    "bandwidth_control.h"
#include    "bandwidth_control_private.h"

#ifdef FLAG_SUPPORT_MODEM_SCALE
//modem speed change
#include    <netutils/ifc.h>
#endif

//#ifdef FLAG_SUPPORT_SMI_SETTING
#if 1
#include    "mt_smi.h" 
#endif

#define MAX_EMI_CTRL_STR_SIZE  (128)

/*=============================================================================
 SMI Bandwidth Information Query
 =============================================================================*/
BWC_MONITOR::BWC_MONITOR(){
    this->smi_fd = -1;
    this->start();
}

BWC_MONITOR::~BWC_MONITOR(){
    this->stop();
}

int BWC_MONITOR::start(){
    if( this->smi_fd == -1 ){
        //BWC_INFO("Start BWC_NONITOR");
        this->smi_fd = open("/dev/MTK_SMI", O_RDONLY);

        if( this->smi_fd == -1 ){
            BWC_ERROR("Open SMI(/dev/MTK_SMI) driver file failed.:%s\n",
                strerror(errno));
            return -1;
        }else{
            return 0;
        }
    }
    return -1;
}

int BWC_MONITOR::stop(){
    if( this->smi_fd != -1 ){
        close( smi_fd);
    }
    //BWC_INFO("Stop BWC_NONITOR");
    this->smi_fd = -1;
    return 0;
}

unsigned int BWC_MONITOR::get_smi_bw_state(){
    // To save the query result
    MTK_SMI_BWC_MM_INFO_USER mm_info = { 0, 0, { 0, 0 }, { 0, 0 }, { 0, 0 }, {
        0, 0 }, 0, 0, 0, 1092 * 1080 * 7};

    if( this->smi_fd == -1 ){
        // Open device again
        this->smi_fd = open("/dev/MTK_SMI", O_RDWR);
        // FD leak may happen 
        if( this->smi_fd == -1 ){
            BWC_INFO("get_smi_bw_state: Open SMI(/dev/MTK_SMI) failed.:%s\n",
                strerror(errno));
            return -1;
        }
    }


    if( ioctl(smi_fd, MTK_IOC_SMI_BWC_INFO_GET, (void *)&mm_info) < 0 ){
        BWC_ERROR("MTK_IOC_SMI_BWC_INFO_GET failed.:%s\n", strerror(errno));
        return -1;
    }

    //BWC_INFO("get_bwc_mm_property success");

    return mm_info.hw_ovl_limit;

}

/*=============================================================================
    SMI Bandwidth Control
  =============================================================================*/

extern "C"
{
/*=============================================================================
    SMI Bandwidth Control
  =============================================================================*/
static MTK_SMI_BWC_SCEN map_bwc_profile_to_smi(BWC_PROFILE_TYPE bwc_profile)
{
    switch (bwc_profile)
    {
        case BWCPT_VIDEO_LIVE_PHOTO:
            return SMI_BWC_SCEN_MM_GPU;

        case BWCPT_VIDEO_WIFI_DISPLAY:
            return SMI_BWC_SCEN_WFD;

        case BWCPT_VIDEO_RECORD:
            return SMI_BWC_SCEN_VENC;

        case BWCPT_VIDEO_RECORD_SLOWMOTION:
            return SMI_BWC_SCEN_VR_SLOW;

        case BWCPT_VIDEO_RECORD_CAMERA:
        case BWCPT_VIDEO_TELEPHONY:
        case BWCPT_CAMERA_ZSD:
        case BWCPT_CAMERA_PREVIEW:
        case BWCPT_VIDEO_SNAPSHOT:
        case BWCPT_CAMERA_CAPTURE:
            return SMI_BWC_SCEN_VR;

        case BWCPT_CAMERA_ICFP:
            return SMI_BWC_SCEN_ICFP;
            
        case BWCPT_VIDEO_SWDEC_PLAYBACK:
            return SMI_BWC_SCEN_SWDEC_VP;
            
        case BWCPT_VIDEO_PLAYBACK:      
            return SMI_BWC_SCEN_VP;
            
        default:
            return SMI_BWC_SCEN_NORMAL;
    }   

    return SMI_BWC_SCEN_NORMAL;
}
}

int BWC::smi_bw_ctrl_set( BWC_PROFILE_TYPE profile_type , BWC_VCODEC_TYPE codec_type , bool bOn )
{
    
//#ifdef FLAG_SUPPORT_SMI_SETTING
#if 1

    int                     smi_fd = -1;    //smi device driver node file descriptor
    MTK_SMI_BWC_CONFIG      cfg;

    smi_fd = open("/dev/MTK_SMI", O_RDONLY);

    if(-1 == smi_fd)
    {
        BWC_ERROR("Open SMI(/dev/MTK_SMI) driver file failed.:%s\n", strerror(errno));
        return -1;
    }

    cfg.b_on_off = ((0 < bOn) ? 1 : 0);
    cfg.scenario = map_bwc_profile_to_smi(profile_type);

    if( ioctl( smi_fd, MTK_IOC_SMI_BWC_CONFIG , &cfg ) < 0 )
    {
        BWC_ERROR("MTK_IOC_SMI_BWC_CONFIG failed.:%s\n" , strerror(errno) );
        close(smi_fd);
        return -1; 
    }

    BWC_INFO("smi_bw_ctrl_set: scen %d, turn %s\n", cfg.scenario, (cfg.b_on_off ? "on" : "off") );

    close(smi_fd);
    
    return 0;

#else
    BWC_INFO("smi_bw_ctrl_set: NULL FUNCTION\n");
    return 0;
#endif
}


/*=============================================================================
    EMI Bandwidth Control
  =============================================================================*/
static int emi_ctrl_str_generate( BWC_PROFILE_TYPE profile_type , BWC_VCODEC_TYPE codec_type, bool bOn, char* out_str )
{
    char *p_cmdstr_profile = NULL;
    char *p_cmdstr_switch  = NULL;
    
    
    switch( profile_type )
    {
    case BWCPT_VIDEO_NORMAL:
    case BWCPT_VIDEO_WIFI_DISPLAY:
    case BWCPT_VIDEO_LIVE_PHOTO:
        p_cmdstr_profile = (char*)"CON_SCE_NORMAL";
        break;
    case BWCPT_VIDEO_RECORD_SLOWMOTION:
    case BWCPT_VIDEO_RECORD_CAMERA:
    case BWCPT_VIDEO_RECORD:
        if( codec_type == BWCVT_MPEG4 ){
            p_cmdstr_profile = (char*)"CON_SCE_VR_MP4";
        }
        else{
            p_cmdstr_profile = (char*)"CON_SCE_VR_H264";
        }
        break;
        
    case BWCPT_VIDEO_PLAYBACK:
    case BWCPT_VIDEO_SWDEC_PLAYBACK:
        p_cmdstr_profile = (char*)"CON_SCE_VP";
        break;
                
    case BWCPT_VIDEO_SNAPSHOT:
        if( codec_type == BWCVT_MPEG4 ){   /*VSS use VR profile*/
            p_cmdstr_profile = (char*)"CON_SCE_VR_MP4"; 
        }
        else{
            p_cmdstr_profile = (char*)"CON_SCE_VR_H264";
        }
        break;
        
    case BWCPT_VIDEO_TELEPHONY:
        p_cmdstr_profile = (char*)"CON_SCE_VC";
        break;
                
    case BWCPT_CAMERA_PREVIEW:
        p_cmdstr_profile = (char*)"CON_SCE_VR_H264"; /* Camera preview use VR */
        break;
        
    case BWCPT_CAMERA_CAPTURE:
    case BWCPT_CAMERA_ICFP:
        p_cmdstr_profile = (char*)"CON_SCE_IC";
        break;

    case BWCPT_CAMERA_ZSD:
        p_cmdstr_profile = (char*)"CON_SCE_ZSD";
        break;
        
    case BWCPT_NONE:
        p_cmdstr_profile = (char*)"CON_SCE_NORMAL";
        break;

    default:
        BWC_ERROR("Invalid profile_type = %d\n", (int)profile_type );
        return -1;
        
    }



    p_cmdstr_switch = ( bOn == true ) ? (char*)" ON":(char*)" OFF";

    strcpy( out_str, p_cmdstr_profile );
    strcat( out_str, p_cmdstr_switch );

    return 0;
    
    
}




int BWC::emi_bw_ctrl_set( BWC_PROFILE_TYPE profile_type , BWC_VCODEC_TYPE codec_type , bool bOn )
{
    const char  *con_sce_file    = "/sys/bus/platform/drivers/mem_bw_ctrl/concurrency_scenario";
    int         fd;
    char        emi_ctrl_str[128];


    if( emi_ctrl_str_generate( profile_type, codec_type, bOn, emi_ctrl_str ) < 0 ){
        BWC_ERROR("emi_ctrl_str_generate failed!\n");
        return -1;
    }
        

    fd = open(con_sce_file, O_WRONLY);
    
    if (fd == -1) 
    {
        BWC_ERROR("fail to open mem_bw_ctrl driver file\n");
        fsync(1);
        return -1;
    } 
    else 
    {
        BWC_INFO("emi_bw_ctrl_set: %s\n", emi_ctrl_str);
        
        /* enable my scenario before running my application*/
        write(fd, emi_ctrl_str, strlen(emi_ctrl_str));

    }

    close(fd);
    
    return 0;
}


/*=============================================================================
    EMI DDR TYPE Get
  =============================================================================*/
BWC::EMI_DDR_TYPE BWC::emi_ddr_type_get( void )
{
    const char      *ddr_type_file    = "/sys/bus/platform/drivers/ddr_type/ddr_type";
    int             fd;
    char            ddr_type_str[128];
    EMI_DDR_TYPE    ddr_type;


    fd = open(ddr_type_file, O_RDONLY);
    
    if (fd == -1) 
    {
        BWC_ERROR("fail to open ddr_type_file driver file\n");
        fsync(1);
        return EDT_NONE;
    } 
    else 
    {
        int i;

        for( i = 0; i < ((int)sizeof(ddr_type_str) - 1); i++ ) {
            if( read(fd, &(ddr_type_str[i]), 1 ) <= 0 ) {
                break;
            }

            if( (ddr_type_str[i] == 0xA) || (ddr_type_str[i] == 0xD) ){
                break;
            }
        }
        
        ddr_type_str[i] = '\0';

        BWC_INFO("Read DDR type string:%s\n", ddr_type_str);
    }

    close(fd);


    //Mapping DDR type
    if( strcmp( ddr_type_str , "LPDDR2" ) == 0 ){
        return EDT_LPDDR2;
    }

    if( strcmp( ddr_type_str , "DDR3" ) == 0 ){
        return EDT_DDR3;
    }
    
    if( strcmp( ddr_type_str , "LPDDR3" ) == 0 ){
        return EDT_LPDDR3;
    }

    if( strcmp( ddr_type_str , "mDDR" ) == 0 ){
        return EDT_mDDR;
    }
    
    return EDT_NONE;
    
}



/*=============================================================================
    Modem Speed Control
  =============================================================================*/

int BWC::modem_speed_profile_set( BWC::MODEM_SPEED_PROFILE profile )
{
    
#ifdef FLAG_SUPPORT_MODEM_SCALE


    switch( profile )
    {
    case MSP_NORMAL:
        BWC_INFO("ifc_set_throttle: %d %d (Normal)\n", -1, -1 );
        ifc_set_throttle( "ccmni0", -1, -1 );       //UnLimit
        break;

    case MSP_SCALE_DOWN:
        BWC_INFO("ifc_set_throttle: %d %d (Scale Down)\n", (int)21*1024, (int)5.7*1024 );
        ifc_set_throttle( "ccmni0", 21*1024, 5.7*1024 );  //Limit downlink to 21Mbps/uplink 5.7Mbps
        break;

    default:
        BWC_ERROR("Unknown modem speed profile:%d\n", profile );
        return -1;
    }



#endif
    
    return 0;
}

/*=============================================================================
 BWCHelper
 =============================================================================*/

int BWCHelper::set_bwc_mm_property( int propterty_id, int value1, int value2 ){
    MTK_SMI_BWC_INFO_SET cfg;
    int smi_fd = -1;

    smi_fd = open("/dev/MTK_SMI", O_RDONLY);

    if( -1 == smi_fd ){
        BWC_ERROR("Open SMI(/dev/MTK_SMI) driver file failed.:%s\n",
            strerror(errno));
        return -1;
    }

    cfg.property = propterty_id;
    cfg.value1 = value1;
    cfg.value2 = value2;

    if( ioctl(smi_fd, MTK_IOC_SMI_BWC_INFO_SET, &cfg) < 0 ){
        BWC_ERROR("MTK_IOC_SMI_BWC_INFO_SET failed.:%s\n", strerror(errno));
        close(smi_fd);
        return -1;
    }

    BWC_INFO("set_bwc_mm_property: propterty_id=%d, value1=%d, value2=%d \n",
        cfg.property, cfg.value1, cfg.value2);

    smi_fd = close(smi_fd);

    if( -1 == smi_fd ){
        BWC_ERROR("Close SMI(/dev/MTK_SMI) driver file failed.:%s\n",
            strerror(errno));
        return -1;
    }

    return 0;

}

int BWCHelper::get_bwc_mm_property( MTK_SMI_BWC_MM_INFO_USER * properties ){

    int smi_fd = -1;

    smi_fd = open("/dev/MTK_SMI", O_RDONLY);

    if( -1 == smi_fd ){
        BWC_ERROR("Open SMI(/dev/MTK_SMI) driver file failed.:%s\n",
            strerror(errno));
        return -1;
    }


    if( ioctl(smi_fd, MTK_IOC_SMI_BWC_INFO_GET, (void *)properties) < 0 ){
        BWC_ERROR("MTK_IOC_SMI_BWC_INFO_GET failed.:%s\n", strerror(errno));
        close(smi_fd);
        return -1;
    }

    BWC_INFO("get_bwc_mm_property success");

    smi_fd = close(smi_fd);

    if( -1 == smi_fd ){
        BWC_ERROR("Close SMI(/dev/MTK_SMI) driver file failed.:%s\n",
            strerror(errno));
        return -1;
    }
    
    return 0;

}

void BWCHelper::profile_add(
    MTK_SMI_BWC_MM_INFO_USER * properties,
    BWC_PROFILE_TYPE profile ){

    properties->concurrent_profile = (properties->concurrent_profile | (1
        << profile));
    this->set_bwc_mm_property(SMI_BWC_USER_INFO_CON_PROFILE,
        properties->concurrent_profile, 0);
}

void BWCHelper::profile_remove(
    MTK_SMI_BWC_MM_INFO_USER * properties,
    BWC_PROFILE_TYPE profile ){

    properties->concurrent_profile = (properties->concurrent_profile & (~(1
        << profile)));
    this->set_bwc_mm_property(SMI_BWC_USER_INFO_CON_PROFILE,
        properties->concurrent_profile, 0);
}

int BWCHelper::profile_get( MTK_SMI_BWC_MM_INFO_USER * properties ){
    return properties->concurrent_profile;

}


extern "C"
{

static unsigned int g_mmdvfs_camera_mode = MMDVFS_CAMERA_MODE_FLAG_DEFAULT;

static void mmdvfs_parse_arg(va_list arg_ptr, MTK_MMDVFS_CMD *cmd)
{
    while (1)
    {
        mmdvfs_parameter_enum type;
        type = (mmdvfs_parameter_enum)va_arg(arg_ptr, int);
        if (type == MMDVFS_PARAMETER_EOF)
        {
            break;
        }
        unsigned int value;

        value = va_arg(arg_ptr, unsigned int);  

        BWC_INFO("MMDVFS v %d %d =%d=\n", type, value, cmd->camera_mode);

        switch (type)
        {
            case MMDVFS_SENSOR_SIZE:
                cmd->sensor_size = value;
                break;
            case MMDVFS_SENSOR_FPS:
                cmd->sensor_fps = value;
                break;
            case MMDVFS_CAMERA_MODE_PIP:
                cmd->camera_mode &= ~MMDVFS_CAMERA_MODE_FLAG_PIP;
                if (value)
                {
                    cmd->camera_mode |= MMDVFS_CAMERA_MODE_FLAG_PIP;
                }                               
                break;
            case MMDVFS_CAMERA_MODE_VFB:
                cmd->camera_mode &= ~MMDVFS_CAMERA_MODE_FLAG_VFB;
                if (value)
                {
                    cmd->camera_mode |= MMDVFS_CAMERA_MODE_FLAG_VFB;
                }                               
                break;
            case MMDVFS_CAMERA_MODE_EIS_2_0:
                cmd->camera_mode &= ~MMDVFS_CAMERA_MODE_FLAG_EIS_2_0;
                if (value)
                {
                    cmd->camera_mode |= MMDVFS_CAMERA_MODE_FLAG_EIS_2_0;
                }                               
                break;
            case MMDVFS_CAMERA_MODE_IVHDR:
                cmd->camera_mode &= ~MMDVFS_CAMERA_MODE_FLAG_IVHDR;
                if (value)
                {
                    cmd->camera_mode |= MMDVFS_CAMERA_MODE_FLAG_IVHDR;
                }                               
                break;
            case MMDVFS_VENC_SIZE:
                cmd->venc_size = value;
                break;
            default:
                BWC_ERROR("unknown parameter %d", type);
            break;
        }
    }
}

static int mmdvfs_smi_fd = -1;

mmdvfs_step_enum mmdvfs_query(BWC_PROFILE_TYPE scenario, ...)
{
    MTK_MMDVFS_CMD cmd;
    memset(&cmd, 0, sizeof(MTK_MMDVFS_CMD));
    cmd.camera_mode = MMDVFS_CAMERA_MODE_FLAG_DEFAULT;

    // parse args
    va_list arg_ptr;
    va_start(arg_ptr, scenario);
    mmdvfs_parse_arg(arg_ptr, &cmd);
    va_end(arg_ptr);

    BWC_INFO("MMDVFS QUERY %d\n", cmd.camera_mode);

    cmd.type = MTK_MMDVFS_CMD_TYPE_QUERY;
    cmd.scen = map_bwc_profile_to_smi(scenario);

    if (mmdvfs_smi_fd == -1)
    {
        mmdvfs_smi_fd = open("/dev/MTK_SMI", O_RDONLY);
    }

    if (ioctl(mmdvfs_smi_fd, MTK_IOC_MMDVFS_CMD, &cmd) < 0)
    {
        BWC_ERROR("MTK_IOC_MMDVFS_CMD failed.:%s\n" , strerror(errno) );
        close(mmdvfs_smi_fd);
        mmdvfs_smi_fd = -1;
    }
    
    return (mmdvfs_step_enum)cmd.ret;
}


int mmdvfs_set(BWC_PROFILE_TYPE scenario, ...)
{
    MTK_MMDVFS_CMD cmd;
    memset(&cmd, 0, sizeof(MTK_MMDVFS_CMD));
    cmd.camera_mode = g_mmdvfs_camera_mode;
       
    // parse args
    va_list arg_ptr;
    va_start(arg_ptr, scenario);
    mmdvfs_parse_arg(arg_ptr, &cmd);
    va_end(arg_ptr);

    g_mmdvfs_camera_mode = cmd.camera_mode;

    BWC_INFO("MMDVFS SET %d\n", g_mmdvfs_camera_mode);

    cmd.type = MTK_MMDVFS_CMD_TYPE_SET;
    cmd.scen = map_bwc_profile_to_smi(scenario);    

    if (mmdvfs_smi_fd == -1)
    {
        mmdvfs_smi_fd = open("/dev/MTK_SMI", O_RDONLY);
    }

    if (ioctl(mmdvfs_smi_fd, MTK_IOC_MMDVFS_CMD, &cmd) < 0)
    {
        BWC_ERROR("MTK_IOC_MMDVFS_CMD failed.:%s\n" , strerror(errno) );
        close(mmdvfs_smi_fd);
        mmdvfs_smi_fd = -1;
        
        return -1; 
    }

    return (int)cmd.ret;
}

}



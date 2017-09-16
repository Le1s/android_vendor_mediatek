/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */


#include <ctype.h>
#include <errno.h>
#include <fcntl.h>
#include <getopt.h>
#include <limits.h>
#include <linux/input.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/reboot.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>
#include <cutils/properties.h>
#include <unistd.h>
#include <pthread.h>

#include "common.h"
#include "ftm.h"
#include "miniui.h"
#include "utils.h"
#include "item.h"

#define BUFSZ      2048 
#define TAG        "[ATA] "

#define START "AT+START"
#define STOP "AT+STOP"
#define REQUEST_DATA "AT+REQUESTDATA"
#define VERSION "AT+VERSION"
#define READ_BARCODE "AT+READBARCODE"
#define WRITE_BARCODE "AT+WRITEBARCODE"
#define VIBRATOR_ENABLE "/sys/class/timed_output/vibrator/enable"
char SP_ATA_PASS[16] = "pass\r\n";
char SP_ATA_FAIL[16] = "fail\r\n";

int bg_arr[MAX_ROWS];
int status = 0;
int item_testing = 0;

#define MAX_RETRY_COUNT 20
#ifdef FEATURE_FTM_VIBRATOR
extern bool vibrator_test_exit;
#endif
#ifdef FEATURE_FTM_LED
extern bool keypadled_test_exit;
extern bool led_test_exit;
#endif
#ifdef FEATURE_FTM_LCD
int lcd_test_exit;
extern int backlight_test_exit;
#endif
#ifdef FEATURE_FTM_AUDIO
extern bool bMicbias_exit;
#endif
#ifdef CUSTOM_KERNEL_ACCELEROMETER
extern bool gsensor_thread_exit;
#endif

#ifdef CUSTOM_KERNEL_MAGNETOMETER
extern bool msensor_thread_exit;
#endif

#if defined(CUSTOM_KERNEL_ALSPS) || defined(CUSTOM_KERNEL_PS) || defined(CUSTOM_KERNEL_ALS)
extern bool alsps_thread_exit;
#endif

#ifdef CUSTOM_KERNEL_GYROSCOPE
extern bool gyroscope_thread_exit;
#endif

#define skip_spaces(source_string_ptr)                                  \
      while( source_string_ptr->string_ptr[ source_string_ptr->cmd_index ]   \
                                 == ' ' )                             \
      {                                                                      \
        source_string_ptr->cmd_index++;                                          \
      }


sp_ata_data return_data;
static int test_item_id = -1, test_item_index = -1;
bool data_ready = false;
static at_cmd g_at_cmd_struct;   /*g_at_cmd_struct  这个变量起到中心通信的作用*/
pthread_mutex_t locker;
pthread_cond_t at_cmd_ready;
pthread_cond_t at_cmd_empty;

static char* ftm_request_data_cb(at_cmd *test_item_struct, char* test_result);
static char* ftm_item_entry_cb(at_cmd *test_item_struct, char* test_result);
static char* ftm_item_add_cb(at_cmd *test_item_struct, char* test_result);
static char* ftm_read_barcode(at_cmd *test_item_struct, char* result);
static char* ftm_write_barcode(at_cmd *test_item_struct, char* result);
void dispatch_data_to_pc_cb(at_cmd *test_item_struct, char* result);
static char* ftm_camera_data(at_cmd *test_item_struct, char* test_result);
static void ftm_set_property(at_cmd *test_item_struct, char *test_result);
static char* ftm_read_bt_addr(at_cmd *test_item_struct, char* test_result);
static char* ftm_write_bt_addr(at_cmd *test_item_struct, char* test_result);
static char* ftm_read_wifi_mac_addr(at_cmd *test_item_struct, char* test_result);
static char* ftm_write_wifi_mac_addr(at_cmd *test_item_struct, char* test_result);

int usb_com_port = -1;
int usb_close = 1;
int idle_current_done = 0;
int usb_status = 0;
int usb_plug_in = 1;

char image_data[BLOCK_SIZE+1];
int total_block = 0;
int current_block = 0;
bool cam_tran_done = true;
long data_pos = 0, lastblock = 0;
FILE *fp = NULL;


static bool exit_pc_control = false;

static ftm_cmd_hdlr other_cmd_hdlr[]={
        {0, ITEM_CUSTOM_START, "AT+START", dispatch_data_to_pc_cb},
        {1, ITEM_CUSTOM_STOP, "AT+STOP", dispatch_data_to_pc_cb},
        {2, ITEM_CUSTOM_REQUESTDATA, "AT+REQUESTDATA", ftm_request_data_cb},
        {3, ITEM_CUSTOM_VERSION, "AT+VERSION", display_version},
        {4, ITEM_CUSTOM_READBARCODE, "AT+READBARCODE", ftm_read_barcode},
        {5, ITEM_CUSTOM_WRITEBARCODE, "AT+BARCODE", ftm_write_barcode},
        {6, ITEM_CUSTOM_CAMERADATA, "AT+CAMERADATA", ftm_camera_data},
        {7, ITEM_CUSTOM_PROPERTY, "AT+PROPERTY", ftm_set_property},
        {8, ITEM_WIFI,  "AT+READBTADDR", ftm_read_bt_addr},
        {9, ITEM_WIFI,  "AT+WRITEBTADDR", ftm_write_bt_addr},
        {10,ITEM_BT,    "AT+READWIFIMAC", ftm_read_wifi_mac_addr},
        {11,ITEM_BT,    "AT+WRITEWIFIMAC", ftm_write_wifi_mac_addr},
        {ITEM_MAX_IDS, ITEM_MAX_IDS, NULL, NULL},
};


static ftm_cmd_hdlr cmd_hdlr[]={
    #ifdef MTK_FM_SUPPORT
    #ifdef FEATURE_FTM_FM
    #ifdef MTK_FM_RX_SUPPORT
        {1, ITEM_FM, "AT+FM", ftm_item_entry_cb},
    #endif
    #endif
    #endif
    
    #ifndef FEATURE_FTM_WIFI_ONLY
    #ifdef FEATURE_FTM_MEMCARD
        {2, ITEM_MEMCARD, "AT+MEMCARD", ftm_item_entry_cb},
    #endif
    
    #ifdef FEATURE_FTM_SIM
        {3, ITEM_SIM, "AT+SIM", ftm_item_entry_cb},
    #endif
    #endif

    #ifdef MTK_GPS_SUPPORT
    #ifdef FEATURE_FTM_GPS
        {4, ITEM_GPS, "AT+GPS", ftm_item_entry_cb},
    #endif
    #endif

    #ifdef FEATURE_FTM_EMMC
        {5, ITEM_EMMC, "AT+EMMC", ftm_item_entry_cb},
    #endif

    #ifdef MTK_WLAN_SUPPORT
    #ifdef FEATURE_FTM_WIFI
        {6, ITEM_WIFI, "AT+WIFI", ftm_item_entry_cb},
    #endif
    #endif

    #ifdef FEATURE_FTM_AUDIO
        {7, ITEM_LOOPBACK_PHONEMICSPK, "AT+RINGTONE", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_3GDATA_SMS
    #elif defined FEATURE_FTM_3GDATA_ONLY
    #elif defined FEATURE_FTM_WIFI_ONLY
    #else
        {8, ITEM_SIGNALTEST, "AT+SIGNALTEST", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_RTC
        {9, ITEM_RTC, "AT+RTC", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_BATTERY
        {10, ITEM_CHARGER, "AT+CHARGER", ftm_item_entry_cb},
    #endif

    #ifdef MTK_BT_SUPPORT
    #ifdef FEATURE_FTM_BT
        {11, ITEM_BT, "AT+BT", ftm_item_entry_cb},
    #endif
    #endif

    #ifdef FEATURE_FTM_MAIN_CAMERA
        {12, ITEM_MAIN_CAMERA, "AT+MAINCAMERA", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_SUB_CAMERA
        {13, ITEM_SUB_CAMERA, "AT+SUBCAMERA", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_KEYS
        {14, ITEM_KEYS, "AT+KEY", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_MATV
        {15, ITEM_MATV_AUTOSCAN, "AT+MATV", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_TOUCH
        {16, ITEM_TOUCH, "AT+MTOUCH", ftm_item_entry_cb},
        {16, ITEM_TOUCH_AUTO, "AT+TOUCH", ftm_item_entry_cb},
    #endif
    
    #ifdef FEATURE_FTM_FLASH
        {17, ITEM_CLRFLASH, "AT+FLASH", ftm_item_entry_cb},
    #elif defined(FEATURE_FTM_EMMC)
        {17, ITEM_CLREMMC, "AT+FLASH", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_VIBRATOR
        {18, ITEM_VIBRATOR, "AT+VIBRATOR", ftm_item_add_cb},
    #endif

    #ifdef FEATURE_FTM_LED
        {19, ITEM_LED, "AT+LED", ftm_item_add_cb},
    #endif
   
    #ifdef FEATURE_FTM_RECEIVER
        {20, ITEM_RECEIVER, "AT+RECEIVER", ftm_item_entry_cb},
    #endif
    
    #ifdef FEATURE_FTM_CMMB
        {21, ITEM_CMMB, "AT+CMMB", ftm_item_entry_cb},
    #endif

    #ifdef CUSTOM_KERNEL_ACCELEROMETER
        {22, ITEM_GSENSOR, "AT+GSENSOR", ftm_item_add_cb},
    #endif

    #ifdef CUSTOM_KERNEL_MAGNETOMETER
        {23, ITEM_MSENSOR, "AT+MSENSOR", ftm_item_add_cb},
    #endif

    #ifdef CUSTOM_KERNEL_ALSPS
        {24, ITEM_ALSPS, "AT+ALSPS", ftm_item_add_cb},
    #endif

    #ifdef CUSTOM_KERNEL_GYROSCOPE
        {25, ITEM_GYROSCOPE, "AT+GYROSCOPE", ftm_item_add_cb},
    #endif

    #ifdef FEATURE_FTM_IDLE
        {26, ITEM_IDLE, "AT+IDLE", ftm_item_entry_cb},
    #endif
        /**
        {27, ITEM_VIBRATOR_PHONE, "AT+PVIBRATOR", ftm_item_entry_cb},
        {28, ITEM_RECEIVER_PHONE, "AT+PRECEIVER", ftm_item_entry_cb},
        {29, ITEM_HEADSET_PHONE, "AT+PHEADSET", ftm_item_entry_cb},
        {30, ITEM_LOOPBACK_PHONEMICSPK_PHONE, "AT+PLOOPBACK", ftm_item_entry_cb},
        **/
        
        {31, ITEM_BACKLIGHT, "AT+BACKLIGHT", ftm_item_add_cb},
        {32, ITEM_LCD, "AT+LCD", ftm_item_add_cb},
    #ifdef FEATURE_FTM_LCM
        {33, ITEM_LCM, "AT+LCM", ftm_item_entry_cb},
    #endif

    #ifdef FEATURE_FTM_HEADSET
        {34, ITEM_HEADSET, "AT+HEADSET", ftm_item_entry_cb},
    #endif

        {ITEM_MAX_IDS, ITEM_MAX_IDS, NULL, NULL},
    
};

int get_is_ata()
{
    LOGD(TAG "status........................... = %d\n", status);
    return status;
}

static void ftm_set_property(at_cmd *test_item_struct, char *test_result)
{
    char *value = NULL, *prop = NULL;
    char param[64] = {0};
    int set_result = 0;
    if((test_item_struct == NULL) || (test_result == NULL))
    {
        return;
    }
    strcpy(param, test_item_struct->param);
    prop = param;
    LOGD(TAG "%s\n", __FUNCTION__);
    LOGD(TAG "%s\n", test_item_struct->param);
    value = strchr(param, ',');

    if(value == NULL)
    {
        strcpy(test_result, "parameter is wrong!\r\n");
        return;
    }
    else
    {
        *value = 0;
        value++;
    }
    LOGD(TAG "prop=%s, value=%s\n", prop, value);
    set_result = ftm_set_prop(prop, value);

    if((set_result == 0) || (set_result == -EEXIST))
    {
        strcpy(test_result, "pass\r\n");
    }
    else
    {
        strcpy(test_result, "fail\r\n");
    }
    LOGD(TAG "%s\n", ftm_get_prop("FMTX.CH1"));
}

void write_data_to_pc(char * result, int length)
{

    int write_len = 0;
    LOGD(TAG "Entry %s\n", __FUNCTION__);
    if(result == NULL)
    {
        return;
    }
    write_len = write(usb_com_port, result, length);
	if(write_len != length)
	{
		LOGD(TAG "write_len=%d, length=%d,write data to pc fail\n", 
                     write_len, length);
	}
    return;
}

/*
 * camera data transfer need confirm with pc side,
 * and then send next data package.
 * raw data file only need open at first at command.
*/
static char* ftm_camera_data(at_cmd *test_item_struct, char* test_result)
{

    unsigned long filesize = -1;
    unsigned long block_size = BLOCK_SIZE;
    char camera_data[BLOCK_SIZE+4*sizeof(int)] = {0}; // sizeof(sp_ata_camera_data)+\r\n
    char *p_temp = NULL;
    //int block = 0;
    int i = 0, j = 0, repeat = 0;
    sp_ata_camera_data camera_struct;
    char checksum = 0;
    char ch = 0;
    if((test_item_struct == NULL) || (test_result == NULL))
    {
        return NULL;
    }
    //memset(camera_data, 0, sizeof(camera_data));

    if(cam_tran_done)
    {
        fp = fopen("/data/acdkCap.jpg", "rb");
        if(fp == NULL)
        {
            LOGD(TAG "Open /data/acdkCap.jpg fail!\n");
            return NULL;
        }
        fseek(fp, 0L, SEEK_END);
        filesize = ftell(fp);

        LOGD(TAG "%d, %ld, %ld, %d\n", block_size, filesize, filesize/block_size, filesize/block_size);

        total_block = filesize / block_size;
        total_block = (filesize % block_size) ? total_block+1 : total_block;
        LOGD(TAG "total_block  = %d\n", total_block);

        lastblock = filesize % block_size;

        rewind(fp);
        cam_tran_done = false;
    }

    p_temp = image_data;

    fseek(fp, data_pos, 0);

    ch = fgetc(fp);

    while((ch != EOF) && (i < BLOCK_SIZE))
    {

       // file cannot find file end flag EOF
        if(current_block == total_block-1)
        {
            if(i >= lastblock)
                break;
        }
       // file cannot find file end flag EOF

        *(p_temp) = ch;
        p_temp++;
        i++;

        ch = fgetc(fp);
    }
                
    rewind(fp);
    LOGD(TAG "After read file\n");                 

    camera_struct.total_block = total_block;
    camera_struct.current_block = current_block;
    camera_struct.block_size = i;
    memcpy(camera_struct.camera_data, image_data, BLOCK_SIZE);
    memcpy(camera_data, &camera_struct, sizeof(camera_struct));
    strcpy(camera_data+i+3*sizeof(int), "\r\n");

    // 14 is 3 integers and \r\n
    int n = write(usb_com_port, camera_data, i+14);
    if(n != (i+14))
    {

		LOGD(TAG "Write test_data fail,%d, %d, %d\n", i, n, sizeof(camera_data));
        n = write(usb_com_port, "\r\n", strlen("\r\n"));
        while((repeat < 10) && n != strlen("\r\n"))
        {
			LOGD(TAG "Write \r\n fail,%d, %d, %d\n", i, n, strlen("\r\n"));
            usleep(50000);
            n = write(usb_com_port, "\r\n", strlen("\r\n"));
        }
  
        if(repeat == 10)
        {
            LOGD(TAG "repeat = 10, %d\n", i);
        }
        else
        {
            LOGD(TAG "Write test_data successfully, %d, %d, %d\n", i, n, i+14);
        }

    } 
    else
    {
    	LOGD(TAG "Write test_data successfully,%d, %d, %d, %d\n", current_block, i, n, i+14);
        current_block++;
        data_pos += i;
    }

    if(current_block == total_block)
    {
        fclose(fp);
        fp = NULL;
        current_block = 0;
        total_block = 0;
        data_pos = 0;
        cam_tran_done = true;
    }
    return NULL;

}

    
void dispatch_data_to_pc_cb(at_cmd *test_item_struct, char* result)
{
    int write_len = 0;
    char temp_buf[2048] = {0};
    LOGD(TAG "Entry %s\n", __FUNCTION__);
    if((test_item_struct == NULL) || (result == NULL))
    {   
        return;
    }
    if(test_item_struct->cmd_type == 1)
    {
        write_data_to_pc(result, strlen(result));
    }
    else
    {
        switch(other_cmd_hdlr[test_item_struct->index].item_id)
        {
            
            case ITEM_CUSTOM_START:
                write_data_to_pc(SP_ATA_PASS, strlen(SP_ATA_PASS));
                usb_status = 1;
                break;
                    
            case ITEM_CUSTOM_STOP:
                LOGD(TAG "AT+STOP\n");
                exit_pc_control = true;
                write_data_to_pc(SP_ATA_PASS, strlen(SP_ATA_PASS));
                break;
            
            default:
                write_data_to_pc(result, strlen(result));
                break;
        }
    }
}

static char* ftm_request_data_cb(at_cmd *test_item_struct, char* test_result)
{
    char temp_buf[2048] = {0};
    LOGD(TAG "Entry %s\n", __FUNCTION__);
    if((test_item_struct == NULL) || (test_result == NULL))
    {
        return NULL;
    }
    memcpy(temp_buf, &return_data, sizeof(return_data));
	strcpy(temp_buf+sizeof(return_data), "\r\n");
    write_data_to_pc(temp_buf, sizeof(return_data)+2);
    return test_result;
}

static char* ftm_item_add_cb(at_cmd *test_item_struct, char* test_result)
{
    char *cmd_cut = NULL;
    
    LOGD(TAG "Entry %s\n", __FUNCTION__);
    if((test_item_struct == NULL) || (test_result == NULL))
    {
        return NULL;
    }

    if(test_item_struct->test_type == 0)      /*表示测项关闭了*/
    {
        switch(cmd_hdlr[test_item_struct->index].item_id)
        {

            case ITEM_VIBRATOR:

                #ifdef FEATURE_FTM_VIBRATOR
				    vibrator_test_exit = true;
                #endif
            
            break;

            case ITEM_LED:

                #ifdef FEATURE_FTM_LED
				    keypadled_test_exit = true;
				    led_test_exit = true;
                #endif

            break;

            case ITEM_MICBIAS:
                #ifdef FEATURE_FTM_AUDIO
                bMicbias_exit = true;
                #endif
                break;

            case ITEM_LCD:

                #ifdef FEATURE_FTM_LCD
				    pthread_cond_signal(&at_cmd_ready);
                    lcd_test_exit = 1;
                #endif

            break;
           case ITEM_BACKLIGHT:
        
               #ifdef FEATURE_FTM_LCD
           pthread_cond_signal(&at_cmd_ready);
                     backlight_test_exit = 1;
               #endif
        
           break;

            case ITEM_GSENSOR:
                #ifdef CUSTOM_KERNEL_ACCELEROMETER
                    gsensor_thread_exit = true;
                #endif
            break;

            case ITEM_MSENSOR:
                #ifdef CUSTOM_KERNEL_MAGNETOMETER
                    msensor_thread_exit = true;
                #endif
            break;

            case ITEM_ALSPS:
                #ifdef CUSTOM_KERNEL_ALSPS
                    alsps_thread_exit = true;
                #endif
            break;

            case ITEM_GYROSCOPE:
                #ifdef CUSTOM_KERNEL_GYROSCOPE
                    gyroscope_thread_exit = true;
                #endif
            break;
        }
        item_testing = 0;
    }
    else
    {
        LOGD(TAG "item_testing=%d\n", item_testing);
        if(item_testing == 0)             
        {
            item_testing = 1;                /*表示又重新开了测项的话*/
            ftm_item_entry_cb(test_item_struct, test_result);
        }
        else
        {
            // backlight and lcd will send more than one at command to test
            // so the data return to pc is OK, but not test result
            strcpy(test_result, "OK\r\n");
            pthread_cond_signal(&at_cmd_ready);
        }
    }
    return test_result;
}


static char* ftm_item_entry_cb(at_cmd *test_item_struct, char* test_result)
{
    item_t *items;
    struct ftm_module *mod;
    struct ftm_param param;
    int item_id = 0, i = 0;
	char result[3][16] = {"not test\r\n", "pass\r\n", "fail\r\n" };

    LOGD(TAG "Entry %s\n", __FUNCTION__);

    if((test_item_struct == NULL) || (test_result == NULL))
    {
        return NULL;
    }

    items = get_item_list();
    item_id = cmd_hdlr[test_item_struct->index].item_id;
    
    mod = ftm_get_module(item_id);
    //item_testing = 1;
    if (mod && mod->visible)
	{
	    LOGD(TAG "Before ftm_entry()\n");
        param.name = get_item_name(items, item_id);
        param.test_type = test_item_struct->test_type;
        ftm_entry(item_id, &param);
    }

    LOGD(TAG "cmd_hdlr[index].item_id = %d\n", item_id);

    switch(item_id)
    {
        case ITEM_SIGNALTEST:
        case ITEM_BT:
        case ITEM_WIFI:
        case ITEM_GPS:
            if(test_item_struct->test_type != 3)
            {
                if((mod->test_result > 0) && (mod->test_result < FTM_TEST_MAX))
                {
                    strcpy(test_result, result[0]);
                    sprintf(test_result, "%d:%s", item_id, result[mod->test_result]);
                }
                else
                {
                    strcpy(test_result, result[mod->test_result]);
                    sprintf(test_result, "%d:%s", item_id, result[mod->test_result]);
                }
            }
            break;
        default:
            if((mod->test_result > 0) && (mod->test_result < FTM_TEST_MAX))
            {
                strcpy(test_result, result[0]);
                sprintf(test_result, "%d:%s", item_id, result[mod->test_result]);
            }
            else
            {
                strcpy(test_result, result[mod->test_result]);
                sprintf(test_result, "%d:%s", item_id, result[mod->test_result]);
            }
        break;
    }

    LOGD(TAG "result[mod->test_result]=%s\n", result[mod->test_result]);

    return test_result;
}


static char* ftm_read_barcode(at_cmd *test_item_struct, char* result)
{

    int ccci = -1, i = 0;
    char tempBuf[128];

    LOGD(TAG "Entry %s\n", __FUNCTION__);
    
    if(result == NULL)
    {
        return NULL;
    }
    
    if(is_support_modem(1))
    {
	    LOGD(TAG "MTK_ENABLE_MD1\n");
      #ifdef MTK_ENABLE_MD1
	    ccci= openDevice();
	    #endif
	    if(-1 == ccci) 
        {
 		    LOGD(TAG "Fail to open CCCI interface\n");
		    return 0;
        }
	    for (i = 0; i<30; i++) 
            usleep(50000); //sleep 1s wait for modem bootup
	    send_at (ccci, "AT\r\n");
	    wait4_ack (ccci, NULL, 3000);
    }
    else if(is_support_modem(2))
    {
        ccci = openDeviceWithDeviceName("/dev/ccci2_tty0");
	    if(-1 == ccci) 
        {
		    LOGD(TAG "Fail to open ttyMT0 interface\n");
		    return 0;
	    }
    }
    getBarcode(ccci,result);
    if(strlen(result) <= 0)
    {
        strcpy(result, "unknown");	
        sprintf (tempBuf, "%d:%s\r\n", other_cmd_hdlr[test_item_struct->index].item_id, result);
    }
    else
    {
        sprintf (tempBuf, "%d:%s\r\n", other_cmd_hdlr[test_item_struct->index].item_id, result);	
    }
    sprintf (result, "%s", tempBuf);
    closeDevice(ccci);
    return result;

}

static char* ftm_write_barcode(at_cmd *test_item_struct, char* test_result)
{

    char buf[128] = {0};

    int ccci = -1, i = 0;
    int result = -1;
    char *barcode = test_item_struct->param;

    LOGD(TAG "Entry %s\n", __FUNCTION__);
    if(barcode == NULL)
    {
        LOGD(TAG "barcode is null!\n");
                    
    }
    else
    {
        LOGD(TAG "%s\n", barcode);
        if(is_support_modem(1))
        {
	        LOGD(TAG "MTK_ENABLE_MD1\n");
          #ifdef MTK_ENABLE_MD1
	        ccci = openDevice();
	        #endif
	        if(-1 == ccci) 
            {
 		        LOGD(TAG "Fail to open CCCI interface\n");
		        return 0;
            }
        }
        else if(is_support_modem(2))
        {

            ccci = openDeviceWithDeviceName("/dev/ccci2_tty0");
	        if(-1 == ccci) 
            {
		        LOGD(TAG "Fail to open ttyMT0 interface\n");
		        return 0;
	        }
        }
	    for (i = 0; i<30; i++) usleep(50000); //sleep 1s wait for modem bootup
	    send_at (ccci, "AT\r\n");
	    wait4_ack (ccci, NULL, 3000);
        sprintf(buf, "AT+EGMR=1,5,\"%s\"\r\n", barcode);
        send_at(ccci, buf);
        LOGD(TAG "before memset\n");
        memset(buf, 0, sizeof(buf));
        LOGD(TAG "after memset\n");
        result = wait4_ack (ccci, NULL, 3000);

        if(result == 0)
        {
            //strcpy(test_result, SP_ATA_PASS);
            sprintf (test_result, "%d:%s", other_cmd_hdlr[test_item_struct->index].item_id, SP_ATA_PASS);
        }
        else
        {
            //strcpy(test_result, SP_ATA_FAIL);
            sprintf (test_result, "%d:%s", other_cmd_hdlr[test_item_struct->index].item_id, SP_ATA_FAIL);
        }
        closeDevice(ccci);
    }
    return buf;
}

static char* ftm_read_bt_addr(at_cmd *test_item_struct, char* test_result)
{
    char bt_addr[16] = {0};
    int read_result = -1;

    LOGD(TAG "Entry %s\n", __FUNCTION__);
    
    read_result = read_bt(bt_addr);
    if((read_result == 0) && (strlen(bt_addr) == 12))
    {
        sprintf (test_result, "%d:%s\r\n", ITEM_BT, bt_addr);
        LOGD(TAG "Read BT Addr ok\n");
    }
    else
    {
        sprintf (test_result, "%d:%s\r\n", ITEM_BT, "fail");
        LOGD(TAG "Read BT Addr fail\n");
    }

    LOGD(TAG "Exit %s\n", __FUNCTION__);
    return test_result;
}

static char* ftm_write_bt_addr(at_cmd *test_item_struct, char* test_result)
{
    int write_result = -1;
    char write_result_str[64] = {0};
    char *bt_addr = test_item_struct->param;

    LOGD(TAG "Entry %s\n", __FUNCTION__);
    if(bt_addr == NULL)
    {
        LOGD(TAG "BT addr is null!\n");      
    }
    else
    {
        write_result = write_bt(bt_addr, write_result_str);
        if(write_result == 0)
        {
            sprintf (test_result, "%d:%s\r\n", ITEM_BT, "pass");
            LOGD(TAG "Write BT Addr ok\n");
        }
        else
        {
            sprintf (test_result, "%d:%s\r\n", ITEM_BT, "fail");
            LOGD(TAG "Write BT Addr fail\n");
        }
    }

    LOGD(TAG "Exit %s\n", __FUNCTION__);
    return test_result;
    
}

static char* ftm_read_wifi_mac_addr(at_cmd *test_item_struct, char* test_result)
{
    char wifi_addr[16] = {0};
    int read_result = -1;

    LOGD(TAG "Entry %s\n", __FUNCTION__);
    
    read_result = read_wifi(wifi_addr);
    if((read_result == 0) && (strlen(wifi_addr) == 12))
    {
        sprintf (test_result, "%d:%s\r\n", ITEM_WIFI, wifi_addr);
        LOGD(TAG "Read wifi mac addr ok\n");
    }
    else
    {
        sprintf (test_result, "%d:%s\r\n", ITEM_WIFI, "fail");
        LOGD(TAG "Read wifi mac addr fail\n");
    }

    LOGD(TAG "Exit %s\n", __FUNCTION__);
    return test_result;
}

static char* ftm_write_wifi_mac_addr(at_cmd *test_item_struct, char* test_result)
{
    int write_result = -1;
    char write_result_str[64] = {0};
    char return_result[16] = {0};
    char *wifi_addr = test_item_struct->param;

    LOGD(TAG "Entry %s\n", __FUNCTION__);
    if(wifi_addr == NULL)
    {
        LOGD(TAG "Wifi addr is null!\n");      
    }
    else
    {
        write_result = write_wifi(wifi_addr, write_result_str);
        if(write_result == 0)
        {
            sprintf (test_result, "%d:%s\r\n", ITEM_WIFI, "pass");
            LOGD(TAG "Write wifi Addr ok\n");
        }
        else
        {
            sprintf (test_result, "%d:%s\r\n", ITEM_WIFI, "fail");
            LOGD(TAG "Write wifi Addr fail\n");
        }
    }

    LOGD(TAG "Exit %s\n", __FUNCTION__);
    return test_result;
}

static bool is_pc_control()
{
    LOGD(TAG "Entry %s\n", __FUNCTION__);
    struct timeval startime, endtime;
	double time_use = 0;
	int read_from_usb = 0;
	char USB_read_buffer[BUFSZ] = {0};
	bool pc_control = false;
	double max_time = 1500000;
	gettimeofday(&startime, 0);
	LOGD(TAG "time_use = %lf\n", time_use);
        // if usb is not plug in, do not need to entry sp ata
	if((!is_usb_state_plugin()) || (usb_com_port == -1))
	{
//		return false;
	}

	while(time_use < max_time)
	{
        if(usb_com_port != -1)
        {
            read_from_usb = read(usb_com_port, USB_read_buffer, sizeof(USB_read_buffer));
            //LOGD(TAG "read_from_usb=%d\n", read_from_usb);
		}
		if(read_from_usb == -1)
		{
			gettimeofday(&endtime, 0);
			time_use = 1000000 * (endtime.tv_sec - startime.tv_sec) +
				endtime.tv_usec - startime.tv_usec;
            continue;
		}
		else if (read_from_usb > 0)
		{
			if(strncmp(USB_read_buffer, START, strlen(START)) == 0)
			{
                LOGD(TAG "start, %s\n", SP_ATA_PASS);
                int len = write(usb_com_port, SP_ATA_PASS, strlen(SP_ATA_PASS));
				if(len != strlen(SP_ATA_PASS))
				{
					LOGD(TAG "write pass fail in is_pc_control");
				}
				else
				{
					LOGD(TAG "write pass in is_pc_control");
					pc_control = true;
                    usb_status = 1;
					break;
				}
			}
		}
	}

	return pc_control;
}

int get_at_cmd_index(at_cmd *at_cmd_struct, ftm_cmd_hdlr *at_cmd_hdlr)
{
    int i = 0;
    LOGD(TAG "Entry %s\n", __FUNCTION__);

    if((at_cmd_struct == NULL) || (at_cmd_hdlr == NULL))
    {
        return -1;
    }

    while(at_cmd_hdlr->item_id != ITEM_MAX_IDS)
    {
        LOGD(TAG "strlen(at_cmd_hdlr->cmd_string)=%d\n", strlen(at_cmd_hdlr->cmd_string));
        LOGD(TAG "strlen(at_cmd_struct->at_cmd_string)=%d\n", strlen(at_cmd_struct->at_cmd_string));
        if((strlen(at_cmd_hdlr->cmd_string) == strlen(at_cmd_struct->at_cmd_string)) 
            && (!strncmp(at_cmd_hdlr->cmd_string, at_cmd_struct->at_cmd_string, strlen(at_cmd_hdlr->cmd_string))))
        {
//            LOGD(TAG "(strlen(at_cmd_hdlr->cmd_string) == strlen(at_cmd_struct->at_cmd_string)\n");
            break;
        }
        i++;
        at_cmd_hdlr++;

    }
    if(at_cmd_hdlr->item_id == ITEM_MAX_IDS)
    {

        at_cmd_struct->index = -1;
    }
    else
    {
        at_cmd_struct->index = i;
    }
    return i;
    
}

int at_command_parser(at_cmd *at_cmd_struct, char *at_command, int *at_command_type, int *index)
{
    ftm_cmd_hdlr *at_cmd_hdlr = cmd_hdlr;
    ftm_cmd_hdlr *other_at_cmd_hdlr = other_cmd_hdlr;
    int len = 0;
    int i = -1, j = 0, k = 0;
    char *cmd_cut = NULL;
//    int at_cmd_type;

    if(at_cmd_struct != NULL)
    {
        LOGD(TAG "at_cmd_struct != NULL\n");
        if(at_command != NULL)
        {
            LOGD(TAG "at_command != NULL\n");
            if(at_command_type != NULL)
            {
                LOGD(TAG "at_command_type != NULL\n");
                if(index != NULL)
                {
                    LOGD(TAG "index != NULL\n");
                }
            }
        }
    }
    else
    {
        LOGD(TAG "at_cmd_struct == NULL\n");
    }

    if((at_cmd_struct == NULL) || (at_command == NULL) || (at_command_type == NULL) || (index == NULL))
    {
        return -1;
    }
    LOGD(TAG "Entry %s\n", __FUNCTION__);
    
    skip_spaces(at_cmd_struct);
    LOGD(TAG "len of at_cmd_struct->at_cmd_string=%d\n",
        strlen(at_cmd_struct->at_cmd_string));

    while((at_cmd_struct->cmd_index < strlen(at_cmd_struct->string_ptr)) 
           && (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] != '=')
           && (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] != '\r'))
    {
        LOGD(TAG "at_command_parser--%c\n", at_cmd_struct->string_ptr[at_cmd_struct->cmd_index]);
        at_cmd_struct->at_cmd_string[k] = at_cmd_struct->string_ptr[at_cmd_struct->cmd_index];
        at_cmd_struct->cmd_index++;
        k++;
        skip_spaces(at_cmd_struct);
    }
    LOGD(TAG "k=%d, at_cmd_struct->cmd_index=%d, len of at_cmd_struct->at_cmd_string=%d\n",
        k, at_cmd_struct->cmd_index, strlen(at_cmd_struct->at_cmd_string));
    get_at_cmd_index(at_cmd_struct, at_cmd_hdlr);
    
    LOGD(TAG "at_cmd_struct->index=%d\n", at_cmd_struct->index);
    
    if(at_cmd_struct->index == -1)
    {
        get_at_cmd_index(at_cmd_struct, other_at_cmd_hdlr/*, *index*/);  /*得知测项的标号*/
        if(at_cmd_struct->index == -1)
        {
            at_cmd_struct->cmd_type = -1; // This item is not a normal factory test item
            return 2;
        }
        else
        {
            at_cmd_struct->cmd_type = 0; // It is not a factory mode test item
        }
    }
    else
    {
        at_cmd_struct->cmd_type = 1;  // It is a factory mode test item
    }
    LOGD(TAG "at_cmd_struct->cmd_index = %d, strlen(at_cmd_struct->at_cmd_string) = %d, at_cmd_struct->cmd_type=%d\n",
        at_cmd_struct->cmd_index, strlen(at_cmd_struct->string_ptr),
        at_cmd_struct->cmd_type);
    
    //if(at_cmd_struct->cmd_index == strlen(at_cmd_struct->string_ptr))
    if((at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] == '\r') || 
        (at_cmd_struct->cmd_index == strlen(at_cmd_struct->string_ptr)))
    {
        // There is no '='
        LOGD(TAG "at_cmd_struct->cmd_index == strlen(at_cmd_struct->at_cmd_string)\n");
        if((!strncmp(at_command, START, strlen(START))) || (!strncmp(at_command, STOP, strlen(STOP))))
        {
            *at_command_type = 0;
        }
        else
        {
            if(item_testing == 1)  /*item_testing  是做什么的？？？？？*/
            {
                *at_command_type = 0;
            }
            else
            {
                *at_command_type = 1;
            }
            if(at_cmd_struct->cmd_type == 0)
            {
                at_cmd_struct->test_type = 0;
            }
            else if(at_cmd_struct->cmd_type == 1)
            {
                at_cmd_struct->test_type = FTM_AUTO_ITEM;
            }
        }
    }
    else if(at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] == '=')
    {
        LOGD(TAG "at_cmd_struct->cmd_index != strlen(at_cmd_struct->at_cmd_string)\n");
        LOGD(TAG "item_testing=%d\n", item_testing);
        if(item_testing == 1)   /*item_testing和at_command_type两个变量是相反的*/
        {
            *at_command_type = 0;
        }
        else
        {
            *at_command_type = 1;
        }
        at_cmd_struct->cmd_index++; // The char after '='
        skip_spaces(at_cmd_struct);

        if(at_cmd_struct->cmd_index >= strlen(at_cmd_struct->string_ptr))
        {
            return 1;
        }

        if((at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] > '0') && 
            (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] < '9'))
        {
            at_cmd_struct->cmd_type = at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] - '0';
        }
        else if(at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] == 'S')// For Vibretor and LED item stop
        {
            *at_command_type = 0;
            at_cmd_struct->test_type = 0;
            return 0;
        }

        at_cmd_struct->cmd_index++; // The char after the first argument
        skip_spaces(at_cmd_struct);

        LOGD(TAG "at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] == ','");

        if((at_cmd_struct->cmd_index < strlen(at_cmd_struct->string_ptr)) &&
           (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] == ','))
        {
            at_cmd_struct->string_ptr++;
        }
        else
        {
            LOGD(TAG "IN else");
            return 1;
        }

        LOGD(TAG "at_cmd_struct->cmd_type = %d\n", at_cmd_struct->cmd_type);

        skip_spaces(at_cmd_struct);// The char after ','
        if(at_cmd_struct->cmd_index > strlen(at_cmd_struct->string_ptr))
        {
            LOGD(TAG "at_cmd_struct->cmd_index > strlen(at_cmd_struct->string_ptr)\n");
            return 1;
        }

        if((at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] < '0') || 
           (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] > '9'))
        {
            LOGD(TAG "the second parameter is illegle\n");
            return 1;
        }
        else
        {
            LOGD(TAG "in else \n");
            while((at_cmd_struct->cmd_index <= strlen(at_cmd_struct->string_ptr)) &&
                  (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] > '0') && 
                  (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] < '9'))
            {
                LOGD(TAG "analyze test_type\n");
                at_cmd_struct->test_type *= 10;
                at_cmd_struct->test_type += at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] - '0';
                at_cmd_struct->cmd_index++;
            }
        }

        LOGD(TAG "at_cmd_struct->test_type = %d, %c\n", at_cmd_struct->test_type, at_cmd_struct->string_ptr[at_cmd_struct->cmd_index]);

//        at_cmd_struct->test_type= at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] - '0';
        //The character after the second parameter must be ',' or '\r'
        // '\r' means the end of at command
        if((at_cmd_struct->cmd_index > strlen(at_cmd_struct->string_ptr)) &&
           (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] != '\r') &&
           (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] != ',') &&
           (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] != 0))
        {
            LOGD(TAG "analyze the char after test_type\n");
            return 1;
        }

        if((at_cmd_struct->test_type == 0) || (item_testing == 1))
        {
            *at_command_type = 0;
        }


        LOGD(TAG "at_cmd_struct->test_type = %d, %c\n", at_cmd_struct->test_type, at_cmd_struct->string_ptr[at_cmd_struct->cmd_index]);

        // Factory mode test items and read items donot need param
        // Only when not factory mode test items and write data need to analyze the third parameter
        if((at_cmd_struct->cmd_type == 0) && (at_cmd_struct->test_type == 1))
        {
            LOGD(TAG "analyze the parameter\n");
            //at_cmd_struct->cmd_index++; // The char after the first argument
            skip_spaces(at_cmd_struct);

            if((at_cmd_struct->cmd_index < strlen(at_cmd_struct->string_ptr)) &&
                (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] == ','))
            {
                LOGD(TAG "analyze ','\n");
                at_cmd_struct->string_ptr++;
            }
            else
            {
                return 1;
            }
        
            skip_spaces(at_cmd_struct);// The char after the second argument

            if((at_cmd_struct->cmd_index < strlen(at_cmd_struct->string_ptr)) &&
                (at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] == '"'))
            {
                LOGD(TAG "analyze '\"'\n");
                at_cmd_struct->string_ptr++;
            }
            else
            {
                return 1;
            }

            while(at_cmd_struct->string_ptr[at_cmd_struct->cmd_index] != '"')
            {
                at_cmd_struct->param[j++] = at_cmd_struct->string_ptr[at_cmd_struct->cmd_index++];
                if(at_cmd_struct->cmd_index >= strlen(at_cmd_struct->string_ptr))
                {   
                    return 1;
                }
            }
            return 0;
        }
    }
    return 0;
    
}

void item_handle_thread()
{
    int item_index = -1;
    at_cmd hdlr_at_cmd_struct;
    char result[128] = {0};
    int i = 0;
    int bg_thrd = 0;
    int has_test = 0;
    
    LOGD(TAG "Entry %s\n", __FUNCTION__);

    while(!exit_pc_control)
    {

        pthread_mutex_lock(&locker);
        while((!data_ready)  && (!exit_pc_control))
        {
             pthread_cond_wait(&at_cmd_ready, &locker);
        }

        if (exit_pc_control)
        {
            break;
        }
        memcpy(&hdlr_at_cmd_struct, &g_at_cmd_struct, sizeof(hdlr_at_cmd_struct));
        memset(&g_at_cmd_struct, 0, sizeof(g_at_cmd_struct));
        data_ready = false;

        pthread_cond_signal(&at_cmd_empty);

        pthread_mutex_unlock(&locker);

        memset(result, 0, sizeof(result));

        LOGD(TAG "Before callback\n");

        if(hdlr_at_cmd_struct.index == -1)
        {
            strcpy(result, "Cannot find the module!\r\n");
        }
        if(hdlr_at_cmd_struct.cmd_type == 0)
        {
            other_cmd_hdlr[hdlr_at_cmd_struct.index].fun(&hdlr_at_cmd_struct, result);
        }
        else if(hdlr_at_cmd_struct.cmd_type == 1)
        {
            cmd_hdlr[hdlr_at_cmd_struct.index].fun(&hdlr_at_cmd_struct, result);
        }
        if(hdlr_at_cmd_struct.test_type == 3)
        {
            i = 0;
            while(bg_arr[i] != -1)
            {
                LOGD(TAG "bg_arr[%d]=%d, cmd_hdlr[%d].item_id=%d\n", i, 
                    bg_arr[i], hdlr_at_cmd_struct.index,
                    cmd_hdlr[hdlr_at_cmd_struct.index].item_id);
                if(bg_arr[i] != cmd_hdlr[hdlr_at_cmd_struct.index].item_id)
                {
                    i++;
                }
                else
                {
                    has_test = 1;
                    break;
                }
            }
            if(has_test != 1)
            {
                bg_arr[i] = cmd_hdlr[hdlr_at_cmd_struct.index].item_id;
                has_test = 0;
            }

        }
            
        LOGD(TAG "After callback");

        while(usb_status != 1)
        {
            sleep(1);
        }
        
        if(hdlr_at_cmd_struct.cmd_type == 0)
        {
            dispatch_data_to_pc_cb(&hdlr_at_cmd_struct, result);
        }
        else if(hdlr_at_cmd_struct.cmd_type == 1)
        {
            if((hdlr_at_cmd_struct.test_type != 3))
            {
                dispatch_data_to_pc_cb(&hdlr_at_cmd_struct, result);
            }
        }
        
        LOGD(TAG "Before write_data_to_pc, the data is %s\n", result);

    }
    pthread_exit(NULL);
    
    LOGD(TAG "Exit %s\n", __FUNCTION__);

}


void read_data_thread()
{
	int read_from_usb = 0;
	char USB_read_buffer[BUFSZ] = {0};
    int at_command_type = 0, at_cmd_id=0;
    char result[32] = {0};
    int index = 0;
    at_cmd at_cmd_struct;
    char at_cmd_string[32] = {0};
    char param[BUFSZ] = {0};
    int parse_result = 0;
    memset(&g_at_cmd_struct, 0, sizeof(g_at_cmd_struct));

    LOGD(TAG "Entry %s\n", __FUNCTION__);

    while(!exit_pc_control)
    {
        if(0 == usb_plug_in)
        {
            // usb need plug out after entry idle current testing
            // but usb doesnot plug out
            usleep(50000);
            LOGD(TAG "usb_plug_in == 0\n");
            continue;
        }
        else if(is_usb_state_plugin() && (usb_com_port == -1))
        {
            LOGD(TAG "is_usb_state_plugin\n");
            // idle current will close usb, so need open usb com port again after test idle current
            if(1 == idle_current_done)
            {
                open_usb();
                idle_current_done = 0;
            }
        }

        if(usb_com_port != -1)
        {
            read_from_usb = read_a_line_for_ata(usb_com_port, USB_read_buffer, sizeof(USB_read_buffer));
	    }
        else
        {
            continue;
	    }

        if(read_from_usb == -1)
	    {
	        LOGD(TAG "read_from_usb == -1\n");
            continue;
	    }
	    else if(read_from_usb > 3)
	    {
	        LOGD(TAG "read_from_usb > 0, len=%d\n", strlen(USB_read_buffer));
            memset(&at_cmd_struct, 0, sizeof(at_cmd_struct));
            memset(&at_cmd_string, 0, sizeof(at_cmd_string));
            memset(param, 0, BUFSZ);
            // string_ptr means the whole string read from USB
            at_cmd_struct.string_ptr = USB_read_buffer;
            // The part before '='
            at_cmd_struct.at_cmd_string = at_cmd_string;
            at_cmd_struct.param = param;

            parse_result = at_command_parser(&at_cmd_struct, USB_read_buffer, &at_command_type, &index);

            if(parse_result == 1)
            {
                LOGD(TAG "The format of at command is illegal!\n");
                continue;
            }
            else if(parse_result == 2)
            {
                strcpy(result, "Cannot find the module!\r\n");
                write_data_to_pc(result, strlen(result));
                continue;
            }

            // special type of at command and old at command without '='
            if(at_command_type == 0)
            {
                LOGD(TAG "at_command_type == 0, the data is %s\n", SP_ATA_PASS);

                if(item_testing == 1)
                {
                    cmd_hdlr[at_cmd_struct.index].fun(&at_cmd_struct, result);
                }
                
                if(at_cmd_struct.cmd_type == 0)
                {
                    // not factory mode test item
                    LOGD(TAG "at_cmd_struct.cmd_type == 0\n");
                    other_cmd_hdlr[at_cmd_struct.index].fun(&at_cmd_struct, result);
                    //write_data_to_pc(result, strlen(result));
                    //dispatch_data_to_pc_cb(other_cmd_hdlr[at_cmd_struct.index].item_id, result);
                }
                else if(at_cmd_struct.cmd_type == 1)
                {
                    // factory mode test item
                    LOGD(TAG "at_cmd_struct.cmd_type == 1\n");
                    /*自动测试的话直接查询完测试种类直接就测试了*/
                    cmd_hdlr[at_cmd_struct.index].fun(&at_cmd_struct, result);
                    if(at_cmd_struct.test_type != 0)
                    {
                        // LED & Vibrator stop need not return result
                        dispatch_data_to_pc_cb(&at_cmd_struct, result);
                    }
                }

                //dispatch_data_to_pc(cmd_hdlr[index].item_id, SP_ATA_PASS);
            }
            else
            {
                pthread_mutex_lock(&locker);
     
                while((data_ready) && (!exit_pc_control))
                {
                    // wait for handle thread read the at command
                    pthread_cond_wait(&at_cmd_empty, &locker);
                }

                memcpy(&g_at_cmd_struct, &at_cmd_struct, sizeof(g_at_cmd_struct));
                data_ready = true;

                pthread_cond_signal(&at_cmd_ready);

                pthread_mutex_unlock(&locker);
            }

      }
        
    }
        pthread_mutex_lock(&locker);

        test_item_index = ITEM_MAX_IDS;

        pthread_cond_signal(&at_cmd_ready);

        pthread_mutex_unlock(&locker);
        LOGD(TAG "exit %s\n", __FUNCTION__);
        pthread_exit(NULL);
    
}

bool at_command_processor()
{
    pthread_t read_thread, item_test_thread;
    int i = 0;
    int usb_open_status = 0;

    LOGD(TAG "Entry %s\n", __FUNCTION__);

    // Must use O_NDELAY, because USB may not send AT+START to the target
    usb_com_port = open("dev/ttyGS0", O_RDWR | O_NOCTTY | O_NDELAY);

    if(usb_com_port == -1)
    {
        return false;
    }

    bool pc_control = is_pc_control();
    if(pc_control)
    {
        // reopen USB, because factory mode need transfer data to PC, need O_SYNC param
        close(usb_com_port);
        usb_com_port = -1;
        usb_open_status = open_usb();
        if(usb_open_status == -1)
        {
            return false;
        }
        status = 1;
        // bg_arr for multi-thread test
        while(i < MAX_ROWS)
        {
            bg_arr[i++] = -1;
        }
        pthread_create(&read_thread, NULL, read_data_thread, usb_com_port);
        pthread_create(&item_test_thread, NULL, item_handle_thread, usb_com_port);
        pthread_join(read_thread, NULL);
        pthread_join(item_test_thread, NULL);
    }
    status = 0;
    return true;
}


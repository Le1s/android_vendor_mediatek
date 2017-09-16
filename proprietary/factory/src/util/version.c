/* Copyright Statement:                                                                                      
*                                                                                                            
* This software/firmware and related documentation ("MediaTek Software") are                                 
* protected under relevant copyright laws. The information contained herein                                  
* is confidential and proprietary to MediaTek Inc. and/or its licensors.                                     
* Without the prior written permission of MediaTek inc. and/or its licensors,                                
* any reproduction, modification, use or disclosure of MediaTek Software,                                    
* and information contained herein, in whole or in part, shall be strictly prohibited.                       
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
                                                                                                             
                                                                                                             
#include "common.h"                                                                                          
#include "ftm.h"                                                                                             
#include "miniui.h"                                                                                          
#include "utils.h"                                                                                           
                                                                                                             
#define TAG        "[VERSION] "                                                                              
                                                                                                             
int g_nr_lines;                                                                                              
extern sp_ata_data return_data;                                                                              
extern char test_data[128];                                                                                  
extern int fd_c2k ;
                                                                                                          
extern int textview_key_handler(int key, void *priv);                                                        
extern int write_test_report(item_t *items, FILE *fp);                                                       
extern char SP_ATA_PASS[16];                                                                                 
//extern char SP_ATA_FAIL[16] = "fail\r\n";                                                                  
extern int wait4_ack3 (const int fd, char *pACK, int timeout, char *buf);    
extern int get_barcode_from_nvram(char *barcode_result);                                
#define MAX_RETRY_COUNT 20                                                                                   
                                                                                                             
void getIMEI(int sim, int fd,char *result)                                                                   
{                                                                                                            
    unsigned int i=0,j=0;                                                                                    
    char *p = NULL;                                                                                          
    char *ptr = NULL;                                                                                        
    char buf[128]={0};                                                                                       
	  strcpy(result, "unknown");                                                                               
	  int count = 0;                                                                                           
    if(sim==1)                                                                                               
    {                                                                                                        
        strcpy(buf, "AT+EGMR=0,7\r\n");	                                                                     
    }                                                                                                        
    else if(sim==2)                                                                                          
    {                                                                                                        
        strcpy(buf, "AT+EGMR=0,10\r\n");	                                                                   
    }                                                                                                        
	  else if(sim==3)                                                                                          
	  {                                                                                                        
	      strcpy(buf, "AT+EGMR=0,11\r\n");	                                                                   
	  }                                                                                                        
	  else                                                                                                     
	  {                                                                                                        
	      strcpy(buf, "AT+EGMR=0,12\r\n");	                                                                   
	  }                                                                                                        
                                                                                                             
	  send_at(fd, buf);                                                                                        
	  memset(buf,'\0',sizeof(buf));                                                                            
	                                                                                                           
	  wait4_ack3 (fd, NULL, 15000, buf);                                                                       
                                                                                                             
	  p = strchr(buf, '\"'); // find the first double quotation mark.                                          
	  if(NULL == p)                                                                                            
	  {                                                                                                        
	      LOGE("get IMEI error,can't find the first colon");                                                   
	      return -1 ;                                                                                          
	  }                                                                                                        
	                                                                                                           
    strcpy(result, ++p);                                                                                     
                                                                                                             
    ptr = strchr(result, '\"');                                                                              
    if (ptr != NULL)                                                                                         
    {                                                                                                        
        *ptr = 0;                                                                                            
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        LOGE("get IMEI error,can't find the second colon") ;	                                               
        return -1 ;                                                                                          
    }                                                                                                        
    if(strlen(result) <= 0)                                                                                  
    {                                                                                                        
        strcpy(result, "unknown");                                                                           
    }                                                                                                        
    LOGE("getIMEI %s",result);                                                                               
}                                                                                                            

void getMEID(int fd, char *result)
{   
	  const int BUF_SIZE = 10000;
	  char buf[BUF_SIZE];
	                                                                                                           
	  int count = 0;
	  char *p = NULL;
    char *ptr = NULL;
    memset(buf,'\0',BUF_SIZE);
	  strcpy(buf, "AT^MEID\r\n");
	  strcpy(result, "unknow\n");
                                                                                                             
    send_at(fd, buf);
    memset(buf,'\0',BUF_SIZE);
                                                                                                             
    wait4_ack3 (fd, NULL, 15000, buf);
                                                                                                             
    LOGD("MEID: %s",buf);
                                                                                                             
    p = strchr(buf, 'x');
    if(NULL == p)
	  {                                                                                                        
	      LOGE("get MEID error,can't find 'X'");
	      return -1 ;                                                                                          
	  }                                                                                                        
    strcpy(result, ++p);
    ptr = strchr(result, '\n');
    if (ptr != NULL)
    {                                                                                                        
        *ptr = 0;                                                                                            
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        LOGE("get MEID error");                                          
        return -1 ;	                                                                                         
    }                                                                                                        
    if(strlen(result) <= 0)                                                                                  
    {                                                                                                        
        strncpy(result, "unknown", strlen("unknown"));                                                       
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        if(result[strlen(result)-1] == '\r')                                                                 
        {                                                                                                    
            result[strlen(result)-1] = 0;                                                                    
        }                                                                                                    
    }                                                                                                        
                                                                                                             
	  LOGD(TAG "getMEID result = %s\n", result);                                                       
	  return 0;                                                                                                                                                                         	
}

void getRFID(int fd, char *result)
{   
	  const int BUF_SIZE = 10000;
	  char buf[BUF_SIZE];
	                                                                                                           
	  int count = 0;
	  char *p = NULL;
    char *ptr = NULL;
    memset(buf,'\0',BUF_SIZE);
	  strcpy(buf, "AT+ERFID\r\n");
	  strcpy(result, "unknow\n");
                                                                                                             
    send_at(fd, buf);
    memset(buf,'\0',BUF_SIZE);
                                                                                                             
    wait4_ack3 (fd, NULL, 15000, buf);
                                                                                                             
    LOGD("RFChipID: %s",buf);
                                                                                                             
    p = strchr(buf, ' ');
    if(NULL == p)
	  {                                                                                                        
	      LOGE("get RFChipID error,can't find ' '");
	      return -1 ;                                                                                          
	  }                                                                                                        
    strcpy(result, ++p);
    ptr = strchr(result, '\n');
    if (ptr != NULL)
    {                                                                                                        
        *ptr = 0;                                                                                            
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        LOGE("get RF chip ID error");                                          
        return -1 ;	                                                                                         
    }                                                                                                        
    if(strlen(result) <= 0)                                                                                  
    {                                                                                                        
        strncpy(result, "unknown", strlen("unknown"));                                                       
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        if(result[strlen(result)-1] == '\r')                                                                 
        {                                                                                                    
            result[strlen(result)-1] = 0;                                                                    
        }                                                                                                    
    }                                                                                                        
                                                                                                             
	  LOGD(TAG "getRF Chip ID result = %s\n", result);                                                       
	  return 0;                                                                                                                                                                         	
}      
                                                                                                   
int getModemVersion(int fd,char *result)                                                                     
{                                                                                                            
	  const int BUF_SIZE = 128;                                                                                
	  char buf[BUF_SIZE];                                                                                      
	                                                                                                           
	  int count = 0;                                                                                           
	  char *p = NULL;                                                                                          
    char *ptr = NULL;                                                                                        
    memset(buf,'\0',BUF_SIZE);                                                                               
	  strcpy(buf, "AT+CGMR\r\n");                                                                              
	  strcpy(result, "unknow\n");                                                                              
                                                                                                             
    send_at(fd, buf);                                                                                        
    memset(buf,'\0',BUF_SIZE);                                                                               
                                                                                                             
    wait4_ack3 (fd, NULL, 15000, buf);                                                                       
                                                                                                             
    LOGD("buf %s",buf);                                                                                      
                                                                                                             
    p = strchr(buf, ' '); // find the first space char.                                                      
    if(NULL == p)                                                                                            
	  {                                                                                                        
	      LOGE("get ModemVersion error,can't find the first space");                                           
	      return -1 ;                                                                                          
	  }                                                                                                        
    strcpy(result, ++p);                                                                                     
                                                                                                             
    ptr = strchr(result, '\n');                                                                              
    if (ptr != NULL)                                                                                         
    {                                                                                                        
        *ptr = 0;                                                                                            
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        LOGE("get ModemVersion error,can't find the second space");                                          
        return -1 ;	                                                                                         
    }                                                                                                        
    if(strlen(result) <= 0)                                                                                  
    {                                                                                                        
        strncpy(result, "unknown", strlen("unknown"));                                                       
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        if(result[strlen(result)-1] == '\r')                                                                 
        {                                                                                                    
            result[strlen(result)-1] = 0;                                                                    
        }                                                                                                    
    }                                                                                                        
                                                                                                             
	  LOGD(TAG "getModemVersion result = %s\n", result);                                                       
	  return 0;                                                                                                
}                                                                                                            
                                                                                                             
int getBarcode(int fd,char *result)                                                                          
{                                                                                                            
	  const int BUF_SIZE = 128;                                                                                
	  char buf[BUF_SIZE];                                                                                      
                                                                                                             
	  int count = 0;                                                                                           
	  char *p = NULL;                                                                                          
    char *ptr = NULL;                                                                                        
                                                                                                             
	  strcpy(buf, "AT+EGMR=0,5\r\n");                                                                          
	  strcpy(result, "unknown");                                                                               
                                                                                                             
	  send_at(fd, buf);                                                                                        
	  memset(buf,'\0',BUF_SIZE);                                                                               
	  wait4_ack3 (fd, NULL, 15000, buf);                                                                       
	  LOGD("buf %s",buf);                                                                                      
                                                                                                             
	  p = strchr(buf, '\"'); // find the first double quotation mark.                                          
	  if(NULL == p)                                                                                            
	  {                                                                                                        
	      LOGE("get Barcode error,can't find the first colon");                                                
	      return -1 ;                                                                                          
	  }                                                                                                        
    strcpy(result, ++p);                                                                                     
    ptr = strchr(result, '\"');                                                                              
    if (ptr != NULL)                                                                                         
    {                                                                                                        
        *ptr = 0;                                                                                            
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        LOGE("get Barcode error,can't find the second colon");                                               
	      return -1 ;	                                                                                         
    }                                                                                                        
    if(strlen(result) <= 0)                                                                                  
    {                                                                                                        
        strcpy(result, "unknown");	                                                                         
    }                                                                                                        
    LOGE("getBarcode result = %s\n", result);                                                                
    return 0;                                                                                                
}                                                                                                            
                                                                                                             
int write_barcode(int fd, char* barcode)                                                                     
{                                                                                                            
    const int BUF_SIZE = 128;                                                                                
    char buf[BUF_SIZE];                                                                                      
    int result;                                                                                              
    if((fd == -1) || (barcode == NULL))                                                                      
    {                                                                                                        
        return 0;                                                                                            
    }                                                                                                        
                                                                                                             
    memset(buf, 0, BUF_SIZE);                                                                                
    sprintf(buf, "AT+EGMR=1,5,\"%s\"\r\n", barcode);                                                         
    send_at(fd, buf);                                                                                        
    LOGD(TAG "before memset\n");                                                                             
    memset(buf, 0, BUF_SIZE);                                                                                
    LOGD(TAG "after memset\n");                                                                              
//    read_ack(fd, buf, BUFSZ);                                                                              
    result = wait4_ack (fd, NULL, 3000);                                                                     
                                                                                                             
    return result;                                                                                           
}                                                                                                            
                                                                                                             
void print_verinfo(char *info, int *len, char *tag, char *msg)                                               
{                                                                                                            
	char buf[256] = {0};                                                                                       
	int _len = 0;                                                                                              
	int tag_len = 0;                                                                                           
                                                                                                             
    if((info == NULL) || (len == NULL) || (tag == NULL) || (msg == NULL))                                    
    {                                                                                                        
        return;                                                                                              
    }                                                                                                        
                                                                                                             
    _len = *len;                                                                                             
    tag_len = strlen(tag);                                                                                   
	int max_len = gr_fb_width() / CHAR_WIDTH *2;                                                               
	int msg_len = strlen(msg);                                                                                 
                                                                                                             
	int buf_len = gr_fb_width() / CHAR_WIDTH;                                                                  
                                                                                                             
	_len += sprintf(info + _len, "%s", tag);                                                                   
	_len += sprintf(info + _len, ": ");                                                                        
                                                                                                             
	if(msg_len>max_len-tag_len-2)                                                                              
    {                                                                                                        
		_len += sprintf(info+_len,"\n    ");                                                                     
		g_nr_lines++;                                                                                            
	}                                                                                                          
                                                                                                             
	while(msg_len>0)                                                                                           
    {                                                                                                        
		buf_len = max_len - 4;                                                                                   
		buf_len = (msg_len > buf_len ? buf_len : msg_len);                                                       
		strncpy(buf, msg, 256);                                                                              
		buf[buf_len] = 0;                                                                                        
                                                                                                             
		_len += sprintf(info + _len, "%s", buf);                                                                 
		_len += sprintf(info + _len, "\n");                                                                      
		g_nr_lines++;                                                                                            
		msg_len-=buf_len;                                                                                        
		msg = &(msg[buf_len]);                                                                                   
		while(msg_len>0 && msg[0]==' ')                                                                          
        {                                                                                                    
			msg_len--;                                                                                             
			msg = &(msg[1]);                                                                                       
		}                                                                                                        
                                                                                                             
		if(msg_len>0)                                                                                            
        {                                                                                                    
			for(buf_len=0; buf_len < 4; buf_len++) buf[buf_len]=' ';                                               
			buf[buf_len]=0;                                                                                        
			//_len += sprintf(info+_len, buf);                                                                     
			// Fix Anroid 2.3 build error                                                                          
			_len += sprintf(info + _len, "%s", buf);                                                               
		}                                                                                                        
                                                                                                             
	}                                                                                                          
	*len = _len;                                                                                               
	//LOGD(TAG "In factory mode: g_nr_lines = %d\n", g_nr_lines);                                              
}                                                                                                                                                                                                                   
                                                                                     
static int create_md_verinfo(char *info, int *len)                                                           
{                                                                                                            
    int ccci_handle[MODEM_MAX_NUM];                                                                          
                                                                                                             
    char imei1[64]={0};                                                                                      
    char imei2[64]={0};                                                                                      
    char imei3[128]={0};                                                                                     
    char imei4[128]={0};
    char meid[128]={0};
    char rfid[128]={0};                                                                                     
    char modem_ver[128] = "unknown";                                                                         
    char modem_ver2[128] = "unknown";                                                                        
    char barcode[128] = "unknown";                                                                           
    char barcode2[128] = "unknown";                                                                          
                                                                                                             
    int not_c2k_modem_number = 0;                                                                                    
    int ccci_status = 0;                                                                                     
    int sdio_status = 0;
                                                                                                             
    not_c2k_modem_number = get_md_count();                                                                           
    ccci_status = open_ccci(ccci_handle); 
    sdio_status = open_SDIO();                                                                   
    if(not_c2k_modem_number == 1)                                                                                    
    {                                                                                                        
        if(ccci_handle[0] != -1)                                                                             
        {                                                                                                    
            send_at (ccci_handle[0], "AT\r\n");                                                              
            wait4_ack (ccci_handle[0], NULL, 3000);                                                          
            getIMEI(1, ccci_handle[0], imei1);                                                               
            #ifdef GEMINI                                                                                    
                getIMEI(2, ccci_handle[0], imei2);                                                           
                #if defined(MTK_GEMINI_3SIM_SUPPORT)                                                         
                    getIMEI(3,ccci_handle[0], imei3);                                                        
                #elif defined(MTK_GEMINI_4SIM_SUPPORT)                                                       
                    getIMEI(3,ccci_handle[0], imei3);                                                        
                    getIMEI(4,ccci_handle[0], imei4);                                                        
                #endif                                                                                       
            #endif                                                                                           
            getModemVersion(ccci_handle[0], modem_ver);                                                      
            getBarcode(ccci_handle[0],barcode);                                                              
        }                                                                                                    
        closeDevice(ccci_handle[0]);                                                                         
    }                                                                                                        
    else if(not_c2k_modem_number == 2)                                                                               
    {                                                                                                        
        if(ccci_handle[0] != -1)                                                                             
        {                                                                                                    
            send_at (ccci_handle[0], "AT\r\n");                                                              
            wait4_ack (ccci_handle[0], NULL, 3000);                                                          
            getIMEI(1, ccci_handle[0], imei1);                                                               
            getModemVersion(ccci_handle[0], modem_ver);                                                      
            getBarcode(ccci_handle[0],barcode);                                                              
            closeDevice(ccci_handle[0]);                                                                     
        }                                                                                                    
        if(ccci_handle[1] != -1)                                                                             
        {                                                                                                    
            send_at (ccci_handle[1], "AT\r\n");                                                              
            wait4_ack (ccci_handle[1], NULL, 3000);                                                          
            getIMEI(1, ccci_handle[1], imei2);                                                               
            getModemVersion(ccci_handle[1], modem_ver2);                                                     
            getBarcode(ccci_handle[1],barcode2);                                                             
            closeDevice(ccci_handle[1]);                                                                     
        }                                                                                                    
    }                                                                                                        
    else if(not_c2k_modem_number == 0)                                                                               
    {                                                                                                        
        LOGD(TAG "not_c2k_modem_number == 0\n");                                                                     
    }    
                                                                                               
    #if defined(FEATURE_FTM_3GDATA_SMS) || defined(FEATURE_FTM_3GDATA_ONLY) || defined(FEATURE_FTM_WIFI_ONLY)
        get_barcode_from_nvram(barcode);                                                                     
    #endif                                                                                                   
    
    if(is_support_modem(4))
    {     
    	  send_at(fd_c2k,"atz\r\n");
        wait4_ack(fd_c2k,NULL,300);
        send_at (fd_c2k, "ate0q0v1\r\n");
        wait4_ack(fd_c2k,NULL,300);
	      getMEID(fd_c2k, meid);
	      getRFID(fd_c2k,rfid);
    } 
                                                                                                            
    #ifdef FEATURE_FTM_3GDATA_SMS                                                                            
    #elif defined FEATURE_FTM_3GDATA_ONLY                                                                    
    #elif defined FEATURE_FTM_WIFI_ONLY                                                                      
    #elif defined GEMINI                                                                                     
        #ifndef EVDO_DT_SUPPORT                                                                              
            print_verinfo(info, len,  "IMEI1       ", imei1);                                                
            print_verinfo(info, len,  "IMEI2       ", imei2);                                                
            #if defined(MTK_GEMINI_3SIM_SUPPORT)                                                             
                print_verinfo(info, len,  "IMEI3       ", imei3);                                            
	        #elif defined(MTK_GEMINI_4SIM_SUPPORT)                                                             
                print_verinfo(info, len,  "IMEI3       ", imei3);                                            
                print_verinfo(info, len,  "IMEI4       ", imei4);                                            
	        #endif                                                                                             
         #else                                                                                               
            print_verinfo(info, len, "IMEI        ", imei1);                                                 
         #endif                                                                                              
    #else                                                                                                    
        print_verinfo(info, len,  "IMEI        ", imei1);                                                    
    #endif  
    
    #if defined(EVDO_DT_SUPPORT) || defined (MTK_ENABLE_MD3)
       print_verinfo(info, len,  "MEID        ", meid);
       print_verinfo(info, len, "RFID        ", rfid);
    #endif
                                                                                                     
    if(not_c2k_modem_number == 1)                                                                                    
    {                                                                                                        
        print_verinfo(info, len,  "Modem Ver.  ", modem_ver);                                                
        sprintf(return_data.version.modem_ver,"%s", modem_ver);                                              
        print_verinfo(info, len,  "Bar code    ", barcode);                                                  
    }                                                                                                        
    else if(not_c2k_modem_number == 2)                                                                               
    {                                                                                                        
        print_verinfo(info, len,  "Modem Ver.  ", modem_ver);                                                
        sprintf(return_data.version.modem_ver,"%s", modem_ver);                                              
        print_verinfo(info, len,  "Modem Ver2.  ", modem_ver2);                                              
        print_verinfo(info, len,  "Bar code    ", barcode);                                                  
        print_verinfo(info, len,  "Bar code2    ", barcode2);                                                
    }                                                                                                        
                                                                                                             
    #if defined(FEATURE_FTM_3GDATA_SMS) || defined(FEATURE_FTM_3GDATA_ONLY) || defined(FEATURE_FTM_WIFI_ONLY)
        print_verinfo(info, len,  "Bar code    ", barcode);                                                  
    #endif                                                                                                   
    return 0;                                                                                                
}                                                                                                            
                                                                                                             
static int create_ap_verinfo(char *info, int *len)                                                           
{                                                                                                            
    char val[128] = {0};                                                                                     
    char kernel_ver[256] = "unknown";                                                                        
    char uboot_build_ver[128]  = "unknown";                                                                  
    int kernel_ver_fd = -1;                                                                                  
    int kernel_cli_fd = -1;                                                                                  
    char buffer[1024] = {0};                                                                                 
    char *ptr= NULL, *pstr = NULL;                                                                           
    int i = 0;                                                                                               
                                                                                                             
    kernel_ver_fd = open("/proc/version",O_RDONLY);                                                          
    if(kernel_ver_fd!=-1)                                                                                    
    {                                                                                                        
        read(kernel_ver_fd, kernel_ver, 256);                                                                
        close(kernel_ver_fd);                                                                                
    }                                                                                                        
                                                                                                             
    kernel_cli_fd = open("/proc/cmdline",O_RDONLY);                                                          
    if(kernel_cli_fd!=-1)                                                                                    
    {                                                                                                        
        read(kernel_cli_fd,buffer,128);                                                                      
        ptr = buffer;                                                                                        
        pstr = strtok(ptr, ", =");                                                                           
        while(pstr != NULL)                                                                                  
        {                                                                                                    
            if(!strcmp(pstr, "uboot_build_ver"))                                                             
            {                                                                                                
                pstr = strtok(NULL, ", =");                                                                  
                strcpy(uboot_build_ver, pstr);                                                               
            }                                                                                                
            pstr = strtok(NULL, ", =");                                                                      
        }                                                                                                    
        close(kernel_cli_fd);                                                                                
    }                                                                                                        
                                                                                                             
    if(uboot_build_ver[strlen(uboot_build_ver)-1]=='\n') uboot_build_ver[strlen(uboot_build_ver)-1]=0;       
    if(kernel_ver[strlen(kernel_ver)-1]=='\n') kernel_ver[strlen(kernel_ver)-1]=0;                           
                                                                                                             
    property_get("ro.mediatek.platform", val, "unknown");                                                    
    print_verinfo(info, len,  "BB Chip     ", val);                                                          
    property_get("ro.product.device", val, "unknown");                                                       
    print_verinfo(info, len,  "MS Board.   ", val);                                                          
                                                                                                             
    property_get("ro.build.date", val, "TBD");                                                               
    print_verinfo(info, len,  "Build Time  ", val);                                                          
                                                                                                             
    ptr = &(kernel_ver[0]);                                                                                  
    for(i=0;i<strlen(kernel_ver);i++)                                                                        
    {                                                                                                        
        if(kernel_ver[i]>='0' && kernel_ver[i]<='9')                                                         
        {                                                                                                    
            ptr = &(kernel_ver[i]);                                                                          
            break;                                                                                           
        }                                                                                                    
    }                                                                                                        
    print_verinfo(info, len,  "Kernel Ver. ", ptr);                                                          
    property_get("ro.build.version.release", val, "unknown");                                                
    print_verinfo(info, len,  "Android Ver.", val);                                                          
    property_get("ro.mediatek.version.release", val, "unknown");                                             
    print_verinfo(info, len,  "SW Ver.     ", val);                                                          
	sprintf(return_data.version.sw_ver,"%s", val);                                                             
	property_get("ro.custom.build.version",val,"unknown");                                                     
	print_verinfo(info, len,  "Custom Build Verno.", val);                                                     
                                                                                                             
    return len;                                                                                              
}                                                                                                            
                                                                                                             
int create_verinfo(char *info, int size)                                                              
{                                                                                                            
                                                                                                             
    int len = 0;                                                                                             
	g_nr_lines = 0;                                                                                            
                                                                                                             
    create_ap_verinfo(info, &len);                                                                           
    create_md_verinfo(info, &len);                                                                           
                                                                                                             
    return 0;                                                                                                
}                                                                                                            
                                                                                                             
char ** trans_verinfo(const char *str, int *line)                                                            
{                                                                                                            
	char **pstrs = NULL;                                                                                       
	int  len     = 0;                                                                                          
	int  row     = 0;                                                                                          
	const char *start;                                                                                         
	const char *end;                                                                                           
                                                                                                             
    if((str == NULL) || (line == NULL))                                                                      
    {                                                                                                        
        return NULL;                                                                                         
    }                                                                                                        
                                                                                                             
    len = strlen(str) + 1;                                                                                   
    start  = str;                                                                                            
    end    = str;                                                                                            
    pstrs = (char**)malloc(g_nr_lines * sizeof(char*));                                                      
                                                                                                             
	if (!pstrs)                                                                                                
    {                                                                                                        
		LOGE("In factory mode: malloc failed\n");                                                                
		return NULL;                                                                                             
	}                                                                                                          
                                                                                                             
	while (len--)                                                                                              
    {                                                                                                        
		if ('\n' == *end)                                                                                        
        {                                                                                                    
			pstrs[row] = (char*)malloc((end - start + 1) * sizeof(char));                                          
                                                                                                             
			if (!pstrs[row])                                                                                       
            {                                                                                                
				LOGE("In factory mode: malloc failed\n");                                                            
				return NULL;                                                                                         
			}                                                                                                      
                                                                                                             
			strncpy(pstrs[row], start, end - start);                                                               
			pstrs[row][end - start] = '\0';                                                                        
			start = end + 1;                                                                                       
			row++;                                                                                                 
		}                                                                                                        
		end++;                                                                                                   
	}                                                                                                          
                                                                                                             
	*line = row;                                                                                               
	return pstrs;                                                                                              
}                                                                                                            
                                                                                                             
void tear_down(char **pstr, int row)                                                                         
{                                                                                                            
    int i;                                                                                                   
    if(pstr == NULL)                                                                                         
    {                                                                                                        
        return;                                                                                              
    }                                                                                                        
    for (i = 0; i < row; i++)                                                                                
    {                                                                                                        
        if (pstr[i])                                                                                         
        {                                                                                                    
            free(pstr[i]);                                                                                   
            pstr[i] = NULL;                                                                                  
        }                                                                                                    
    }                                                                                                        
	                                                                                                           
    if (pstr)                                                                                                
    {                                                                                                        
        free(pstr);                                                                                          
        pstr = NULL;                                                                                         
    }                                                                                                        
}                                                                                                            
                                                                                                             
                                                                                                             
/*                                                                                                           
    autoreturn:  if the function called by ata, then true;                                                   
    if called by main, then false;                                                                           
*/                                                                                                           
char* display_version_ata(int index, char* result)                                                           
{                                                                                                            
    if(result == NULL)                                                                                       
    {                                                                                                        
        return NULL;                                                                                         
    }                                                                                                        
    return display_version(index, result, true);	                                                           
}                                                                                                            
                                                                                                             
char* display_version(int index, char* result, bool autoreturn)                                              
{                                                                                                            
	char *buf = NULL;                                                                                          
	struct textview vi;	 /* version info */                                                                    
	text_t vi_title;                                                                                           
	int nr_line;                                                                                               
	text_t info;                                                                                               
	int avail_lines = 0;                                                                                       
	text_t rbtn;                                                                                               
                                                                                                             
	buf = malloc(BUFSZ);                                                                                       
                                                                                                             
	init_text(&vi_title, uistr_version, COLOR_YELLOW);                                                         
	init_text(&info, buf, COLOR_YELLOW);                                                                       
	init_text(&info, buf, COLOR_YELLOW);                                                                       
                                                                                                             
	avail_lines = get_avail_textline();                                                                        
	init_text(&rbtn, uistr_key_back, COLOR_YELLOW);                                                            
	ui_init_textview(&vi, textview_key_handler, &vi);                                                          
	vi.set_btn(&vi, NULL, NULL, &rbtn);                                                                        
  create_verinfo(buf, BUFSZ);                                                                                
	LOGD(TAG "after create_verinfo");                                                                          
	vi.set_title(&vi, &vi_title);                                                                              
	vi.set_text(&vi, &info);                                                                                   
	vi.m_pstr = trans_verinfo(info.string, &nr_line);                                                          
	vi.m_nr_lines = g_nr_lines;                                                                                
	LOGD(TAG "g_nr_lines is %d, avail_lines is %d\n", g_nr_lines, avail_lines);                                
	vi.m_start = 0;                                                                                            
	vi.m_end = (nr_line < avail_lines ? nr_line : avail_lines);                                                
	LOGD(TAG "vi.m_end is %d\n", vi.m_end);                                                                    
                                                                                                             
    if(autoreturn)                                                                                           
    {                                                                                                        
    	vi.redraw(&vi);                                                                                        
        strcpy(result, SP_ATA_PASS);                                                                         
    }                                                                                                        
    else                                                                                                     
    {                                                                                                        
        vi.run(&vi);                                                                                         
    }                                                                                                        
                                                                                                             
	LOGD(TAG "Before tear_down\n");                                                                            
	tear_down(vi.m_pstr, nr_line);                                                                             
	if (buf)                                                                                                   
    {                                                                                                        
		free(buf);                                                                                               
        buf = NULL;                                                                                          
    }                                                                                                        
    LOGD(TAG "End of %s\n", __FUNCTION__);                                                                   
                                                                                                             
    return SP_ATA_PASS;                                                                                      
}                                                                                                            
                                                                                                             

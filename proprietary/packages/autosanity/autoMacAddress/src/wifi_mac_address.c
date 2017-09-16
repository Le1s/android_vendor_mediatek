#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <pthread.h>
#include <termios.h>

#include "libnvram.h"

#include "CFG_file_lid.h"
#include "CFG_PRODUCT_INFO_File.h"
#include "Custom_NvRam_LID.h"
#include "CFG_Wifi_File.h"
#include "CFG_BT_File.h"


#define TAG "[Wi-Fi Mac Address]"


#define BUF_SIZE 128
#define HALT_INTERVAL 20000

#define wifi_length 6
#define bt_length 6

WIFI_CFG_PARAM_STRUCT g_wifi_nvram;
ap_nvram_btradio_mt6610_struct g_bt_nvram;
F_ID nvram_fd = {0};

static int convStrtoHex(char*  szStr, unsigned char* pbOutput, int dwMaxOutputLen, int*  pdwOutputLen);
int rmmi_eabt_hdlr ( char *addr)
{
    char output[bt_length] = {0};
    int rec_size = 0;
    int rec_num = 0;
	unsigned char w_bt[bt_length];
    int length;
    int ret, i = 0;
    char value[13] = {0};
    memset(value, 0, sizeof(value));

    nvram_fd = NVM_GetFileDesc(AP_CFG_RDEB_FILE_BT_ADDR_LID, &rec_size, &rec_num, ISREAD);
    printf("rec_size=%d,rec_num=%d\n",rec_size,rec_num);
    if(1 != rec_num)
   {
   	printf("error:unexpected record num %d\n",rec_num);

   	return -1;
   }
   if(sizeof(g_bt_nvram) != rec_size)
   {
   	printf("error:unexpected record size %d\n",rec_size);
   	return -1;
   }
   memset(&g_bt_nvram,0,rec_num*rec_size);
   ret = read(nvram_fd.iFileDesc, &g_bt_nvram, rec_num*rec_size);
   if(-1 == ret||rec_num*rec_size != ret)
   {
   	printf("error:read bt addr fail!/n");
   	return -1;
   }
   printf("read pre bt addr:%02x%02x%02x%02x%02x%02x\n", 
                   g_bt_nvram.addr[0], g_bt_nvram.addr[1], g_bt_nvram.addr[2], g_bt_nvram.addr[3], g_bt_nvram.addr[4], g_bt_nvram.addr[5] 
   );
   NVM_CloseFileDesc(nvram_fd);
    if(strcmp(addr,"read")==0){
        printf("Success: only read bt address done!\n");
        return 0;
    }
   nvram_fd = NVM_GetFileDesc(AP_CFG_RDEB_FILE_BT_ADDR_LID, &rec_size, &rec_num, ISWRITE);
   printf("rec_size=%d,rec_num=%d\n",rec_size,rec_num);
   if(1 != rec_num)
   {
   	printf("error:unexpected record num %d\n",rec_num);
   	return -1;
   }
   if(sizeof(g_bt_nvram) != rec_size)
   {
   	printf("error:unexpected record size %d\n",rec_size);
   	return -1;
   }
   memset(g_bt_nvram.addr,0,bt_length);
   memset(w_bt,0,bt_length);
   length = strlen(addr);
   if(length != 12)
   {
	printf("error:bt address length is not right!\n");
	return -1;
    }
    ret = convStrtoHex(addr,output,bt_length,&length);
    if(-1 == ret)
    {
	printf("error:convert bt address to hex fail\n");
	return -1;
    }
    else
    {
          printf("BT Address:%s\n", output);
     }
     for(i=0;i<bt_length;i++)
     {	
   	    g_bt_nvram.addr[i] = output[i];
     }
     printf("write bt addr:%02x%02x%02x%02x%02x%02x, value:%02x%02x%02x%02x%02x%02x\n", 
                    g_bt_nvram.addr[0], g_bt_nvram.addr[1], g_bt_nvram.addr[2], g_bt_nvram.addr[3], g_bt_nvram.addr[4], g_bt_nvram.addr[5],
                    output[0], output[1], output[2], output[3], output[4], output[5]
     );
     ret = write(nvram_fd.iFileDesc, &g_bt_nvram , rec_num*rec_size);
     if(-1 == ret||rec_num*rec_size != ret)
     {
   	  printf("error:write wifi addr fail!\n");
   	  return -1;
    }
    NVM_CloseFileDesc(nvram_fd);
    printf("write bt addr success!\n");
    if(FileOp_BackupToBinRegion_All())
    {
        printf("backup nvram data to nvram binregion success!\n");
    }
    else
    {
        printf("error:backup nvram data to nvram binregion fail!\n");
   	 return -1;
    }
    sync();
    return 0;
}

// wifi mac
int rmmi_eawifi_hdlr (char *addr)
{
    char output[wifi_length] = {0};
    int rec_size = 0;
    int rec_num = 0;
	unsigned char w_wifi[wifi_length];
    int ret, length = 0, i = 0;
    char* value = addr;
    printf("rmmi_eawifi_hdlr:RMMI_SET_OR_EXECUTE_MODE\n");
    nvram_fd = NVM_GetFileDesc(AP_CFG_RDEB_FILE_WIFI_LID, &rec_size, &rec_num, ISREAD);
    printf("rec_size=%d,rec_num=%d\n",rec_size,rec_num);
	if(1 != rec_num)
	{
		printf("error:unexpected record num %d\n",rec_num);
		return -1;
	}
	if(sizeof(WIFI_CFG_PARAM_STRUCT) != rec_size)
	{
		printf("error:unexpected record size %d\n",rec_size);
		return -1;
	}
	memset(&g_wifi_nvram,0,rec_num*rec_size);
	ret = read(nvram_fd.iFileDesc, &g_wifi_nvram, rec_num*rec_size);
	if(-1 == ret||rec_num*rec_size != ret)
	{
		printf("error:read wifi mac addr fail!/n");
		return -1;
	}
	printf("read wifi addr:%02x%02x%02x%02x%02x%02x\n",
            g_wifi_nvram.aucMacAddress[0], g_wifi_nvram.aucMacAddress[1], g_wifi_nvram.aucMacAddress[2], g_wifi_nvram.aucMacAddress[3], g_wifi_nvram.aucMacAddress[4],
	g_wifi_nvram.aucMacAddress[5]);
	NVM_CloseFileDesc(nvram_fd);
    if(strcmp(addr,"read")==0){
        printf("Success: only read wifi address done!\n");
        return 0;
    }
    nvram_fd = NVM_GetFileDesc(AP_CFG_RDEB_FILE_WIFI_LID, &rec_size, &rec_num, ISWRITE);
		printf("rec_size=%d,rec_num=%d\n",rec_size,rec_num);
		if(1 != rec_num)
		{
			printf("error:unexpected record num %d\n",rec_num);
			return -1;
		}
		if(sizeof(g_wifi_nvram) != rec_size)
		{
			printf("error:unexpected record size %d\n",rec_size);
			return -1;
		}
		memset(g_wifi_nvram.aucMacAddress,0,bt_length);
//   			memset(w_bt,0,bt_length);
    length = strlen(value);
    if(length != 12)
    {
	    printf("error:bt address length is not right!\n");
	    return -1;
    }
    ret = convStrtoHex(value,output,wifi_length,&length);
    if(-1 == ret)
    {
	    printf("error:convert wifi address to hex fail\n");
	    return -1;
    }
    else
    {
        printf("WIFI Address:%s\n", output);
    }
		for(i=0;i<bt_length;i++)
		{
			g_wifi_nvram.aucMacAddress[i] = output[i];
		}
		printf("write wifi addr:%02x%02x%02x%02x%02x%02x, value:%02x%02x%02x%02x%02x%02x\n",
            g_wifi_nvram.aucMacAddress[0], g_wifi_nvram.aucMacAddress[1],
            g_wifi_nvram.aucMacAddress[2], g_wifi_nvram.aucMacAddress[3],
            g_wifi_nvram.aucMacAddress[4], g_wifi_nvram.aucMacAddress[5],
            output[0], output[1], output[2], output[3], output[4], output[5]
            );
		ret = write(nvram_fd.iFileDesc, &g_wifi_nvram , rec_num*rec_size);
		if(-1 == ret||rec_num*rec_size != ret)
		{
			printf("error:write wifi addr fail!\n");
			return -1;
		}
		NVM_CloseFileDesc(nvram_fd);
		printf("Success : write wifi addr done!\n");
		if(FileOp_BackupToBinRegion_All())
		{
			printf("backup nvram data to nvram binregion success!\n");
		}
		else
		{
			printf("error:backup nvram data to nvram binregion fail!\n");
			return -1;
		}
		sync();
    return 0;
}
static int convStrtoHex(char*  szStr, unsigned char* pbOutput, int dwMaxOutputLen, int*  pdwOutputLen){
    printf("Entry %s\n", __FUNCTION__);

    int   dwStrLen;
    int   i = 0;
    unsigned char ucValue = 0;
    printf("before strlen,dwStrLen\n");
    while(szStr[i] != '\0')
    {
        printf("szStr[%d]:%c", i, szStr[i]);
        i++;
    }

    printf("after while loop\n");
    dwStrLen = strlen(szStr);
	printf("after strlen, dwStrLen = %d\n", dwStrLen);

    if(dwMaxOutputLen < dwStrLen/2){
        return -1;
    }
    i = 0;
    for (i = 0; i < dwStrLen; i ++){

    printf("in for loop %c\n", szStr[i]);
        switch(szStr[i]){
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                ucValue = (ucValue * 16) + (szStr[i] -  '0');
                break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                ucValue = (ucValue * 16) + (szStr[i] -  'a' + 10);
                break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                ucValue = (ucValue * 16) + (szStr[i] -  'A' + 10);
                break;
            default:
                return -1;
                break;
        }

        if(i & 0x01){
            pbOutput[i/2] = ucValue;
            printf("int pbOutput:%d, ucValue:%d\n", pbOutput[i/2], ucValue);
            printf("int pbOutput:%02x, ucValue:%02x\n", pbOutput[i/2], ucValue);
            ucValue = 0;
        }
    }

    *pdwOutputLen = i/2;
    printf("Leave %s\n", __FUNCTION__);

    return 0;
}

int main(int argc, char* argv[])
{
    char result[32] = "0021efdacf9b";
	char resultbt[32] = "FEefdacf9b9D";
	int i = 0;
    if (argc > 0) {
        memset(result,0,32);
        strcpy(result,argv[1]);
		memset(resultbt,0,32);
        strcpy(resultbt,argv[1]);
    }
	if (strlen(resultbt)>11){
		resultbt[0] ='F';
		resultbt[1] = 'E';
		for(i=0;i<8;i++){
		   resultbt[i+2] = resultbt[i+4];
		}
		resultbt[10] ='9';
		resultbt[11] = 'D';
	}
    printf("Will write wifi mac as : %s\n",result);
	printf("Will write bt mac as : %s\n",resultbt);
    rmmi_eawifi_hdlr(result);
    rmmi_eabt_hdlr(resultbt);
    return 0;
}


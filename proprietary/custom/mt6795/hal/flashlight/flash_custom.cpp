#define LOG_TAG "flash_custom.cpp"
#include "camera_custom_nvram.h"
#include "camera_custom_types.h"
#include "camera_custom_AEPlinetable.h"
#include "camera_custom_nvram.h"
#include <cutils/xlog.h>
#include "flash_feature.h"
#include "flash_param.h"
#include "flash_tuning_custom.h"
#include <kd_camera_feature.h>



static int g_mainId=1;
static int g_subId=1;

void cust_setFlashPartId_main(int id)
{
	g_mainId=id;
}
void cust_setFlashPartId_sub(int id)
{
	g_subId=id;
}

void cust_setFlashPartId(int sensorDev, int id)
{
    if(sensorDev==DUAL_CAMERA_MAIN_SENSOR)
        g_mainId=id;
    else
        g_subId=id;
}
//FLASH_PROJECT_PARA& cust_getFlashProjectPara_mainV2(int AEMode, NVRAM_CAMERA_STROBE_STRUCT* nvrame)
FLASH_PROJECT_PARA& cust_getFlashProjectPara_V2(int sensorDev, int AEScene, NVRAM_CAMERA_STROBE_STRUCT* nvrame)
{

	if(sensorDev==DUAL_CAMERA_MAIN_SENSOR)
	{
		if(g_mainId==1)
			return cust_getFlashProjectPara(AEScene, 0, nvrame);
		else //if(id==2)
			return cust_getFlashProjectPara_main2(AEScene, 0, nvrame);
	}
	else
	{
		if(g_subId==1)
			return cust_getFlashProjectPara_sub(AEScene, 0, nvrame);
		else //if(id==2)
			return cust_getFlashProjectPara_sub2(AEScene, 0, nvrame);
	}
}


FlashIMapFP cust_getFlashIMapFunc_main();
FlashIMapFP cust_getFlashIMapFunc_sub();
FlashIMapFP cust_getFlashIMapFunc_main2();
FlashIMapFP cust_getFlashIMapFunc_sub2();
FlashIMapFP cust_getFlashIMapFunc(int sensorDev)
{
    if(sensorDev==DUAL_CAMERA_MAIN_SENSOR)
	{
		if(g_mainId==1)
			return cust_getFlashIMapFunc_main();
		else //if(id==2)
			return cust_getFlashIMapFunc_main2();
	}
	else
	{
		if(g_subId==1)
			return cust_getFlashIMapFunc_sub();
		else //if(id==2)
			return cust_getFlashIMapFunc_sub2();
	}

}


FLASH_PROJECT_PARA& cust_getFlashProjectPara_V3(int sensorDev, int AEScene, int isForceFlash, NVRAM_CAMERA_STROBE_STRUCT* nvrame)
{
    if(sensorDev==DUAL_CAMERA_MAIN_SENSOR)
	{
		if(g_mainId==1)
			return cust_getFlashProjectPara(AEScene, isForceFlash, nvrame);
		else //if(id==2)
			return cust_getFlashProjectPara_main2(AEScene, isForceFlash, nvrame);
	}
	else
	{
		if(g_subId==1)
			return cust_getFlashProjectPara_sub(AEScene, isForceFlash, nvrame);
		else //if(id==2)
			return cust_getFlashProjectPara_sub2(AEScene, isForceFlash, nvrame);
	}

}

int cust_getDefaultStrobeNVRam_V2(int sensorDev, void* data, int* ret_size)
{
    /*
	if(sensorDev==DUAL_CAMERA_MAIN_SENSOR)
	{
		if(g_mainId==1)
		{
			XLOGD("devid main id1");
			return getDefaultStrobeNVRam(DUAL_CAMERA_MAIN_SENSOR, data, ret_size);
		}
		else
		{
			XLOGD("devid main id2");
			return getDefaultStrobeNVRam_main2(data, ret_size);
		}
	}
	else
	{
		if(g_subId==1)
		{
			XLOGD("devid sub id1");
			return getDefaultStrobeNVRam(DUAL_CAMERA_SUB_SENSOR, data, ret_size);
		}
		else
		{
			XLOGD("devid sub id2");
			return getDefaultStrobeNVRam_sub2(data, ret_size);
		}

	}
	*/
	return 0;
}

int cust_fillDefaultStrobeNVRam(int sensorDev, void* data)
{
    if(sensorDev==DUAL_CAMERA_MAIN_SENSOR)
	{
		if(g_mainId==1)
		{
			XLOGD("devid main id1");
			return cust_fillDefaultStrobeNVRam_main(data);
		}
		else
		{
			XLOGD("devid main id2");
			return cust_fillDefaultStrobeNVRam_main2(data);
		}
	}
	else
	{
		if(g_subId==1)
		{
			XLOGD("devid sub id1");
			return cust_fillDefaultStrobeNVRam_sub(data);
		}
		else
		{
			XLOGD("devid sub id2");
			return cust_fillDefaultStrobeNVRam_sub2(data);
		}

	}

}

int cust_isDualFlashSupport(int sensorDev)
{
    if(sensorDev == DUAL_CAMERA_MAIN_SENSOR)
        return 1;
    else if(sensorDev == DUAL_CAMERA_SUB_SENSOR)
        return 0;
    return 0;
}

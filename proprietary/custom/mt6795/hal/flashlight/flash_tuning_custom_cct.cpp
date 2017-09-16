#define LOG_TAG "flash_tuning_custom_cct.cpp"
#include "string.h"
#include "camera_custom_nvram.h"
#include "camera_custom_types.h"
#include "camera_custom_AEPlinetable.h"
#include <cutils/xlog.h>
#include "flash_feature.h"
#include "flash_param.h"
#include "flash_tuning_custom.h"
#include <kd_camera_feature.h>

//==============================================================================
//
//==============================================================================
int cust_fillDefaultStrobeNVRam_main (void* data)
{
    int i;
	NVRAM_CAMERA_STROBE_STRUCT* p;
	p = (NVRAM_CAMERA_STROBE_STRUCT*)data;

    static short engTab[]=    {          
		-1, 816,1573,2279,2939,3550,4130,4680,5190,5623,6092,6493,6890,7256,7615,7961,8299,         
		802,1633,2399,3101,3752,4360,4938,5486,5995,6417,6870,7267,7680,8041,8405,8801,  -1,        
		1545,2389,3148,3846,4498,5105,5678,6220,6734,7139,7583,8041,8409,8778,9114,  -1,  -1,        
		2242,3079,3838,4536,5186,5793,6360,6900,7398,7812,8239,8666,9064,9378,  -1,  -1,  -1,        
		2891,3728,4484,5181,5823,6430,7010,7558,8041,8445,8891,9278,9601,  -1,  -1,  -1,  -1,        
		3498,4336,5078,5787,6430,7027,7594,8125,8624,9026,9458,9705,  -1,  -1,  -1,  -1,  -1,        
		4069,4906,5659,6354,6997,7600,8155,8714,9207,9581,9931,  -1,  -1,  -1,  -1,  -1,  -1,        
		4638,5468,6227,6911,7553,8161,8710,9230,9698,9965,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		5153,5996,6764,7423,8068,8652,9111,9682,9999,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		5597,6408,7150,7848,8468,9043,9596,9958,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		6042,6891,7594,8273,8900,9477,9934,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		6488,7267,8014,8707,9320,9837,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		6861,7673,8325,9094,9692,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		7235,8067,8842,9492,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		7646,8471,9185,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		8008,8770,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,        
		8381,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,    
	};
    //{ 892,1284,1653,2338,2966,3564,4103,4573,4972,5260};

	//version
	p->u4Version = NVRAM_CAMERA_STROBE_FILE_VERSION;
	//eng tab
	memcpy(p->engTab.yTab, engTab, sizeof(engTab));

	//tuningPara[8];
	for(i=0;i<8;i++)
    {
        p->tuningPara[i].yTarget = 188;
        p->tuningPara[i].fgWIncreaseLevelbySize = 10;
        p->tuningPara[i].fgWIncreaseLevelbyRef = 0;
        p->tuningPara[i].ambientRefAccuracyRatio = 5;
        p->tuningPara[i].flashRefAccuracyRatio = 1;
        p->tuningPara[i].backlightAccuracyRatio = 18;
        p->tuningPara[i].backlightUnderY = 40;
        p->tuningPara[i].backlightWeakRefRatio = 32;
        p->tuningPara[i].safetyExp =33322;
        p->tuningPara[i].maxUsableISO = 680;
        p->tuningPara[i].yTargetWeight = 0;
        p->tuningPara[i].lowReflectanceThreshold = 13;
        p->tuningPara[i].flashReflectanceWeight = 0;
        p->tuningPara[i].bgSuppressMaxDecreaseEV = 20;
        p->tuningPara[i].bgSuppressMaxOverExpRatio = 6;
        p->tuningPara[i].fgEnhanceMaxIncreaseEV = 50;
        p->tuningPara[i].fgEnhanceMaxOverExpRatio = 6;
        p->tuningPara[i].isFollowCapPline = 1;
        p->tuningPara[i].histStretchMaxFgYTarget = 300;//285;//266;
        p->tuningPara[i].histStretchBrightestYTarget = 480;//404;//328;
        p->tuningPara[i].fgSizeShiftRatio = 0;
        p->tuningPara[i].backlitPreflashTriggerLV = 90;
        p->tuningPara[i].backlitMinYTarget = 100;
    }

    p->tuningPara[0].isFollowCapPline = 0;

    p->paraIdxForceOn[0] =1;    //default
    p->paraIdxForceOn[1] =0;    //LIB3A_AE_SCENE_OFF
    p->paraIdxForceOn[2] =0;    //LIB3A_AE_SCENE_AUTO
    p->paraIdxForceOn[3] =1;    //LIB3A_AE_SCENE_NIGHT
    p->paraIdxForceOn[4] =1;    //LIB3A_AE_SCENE_ACTION
    p->paraIdxForceOn[5] =1;    //LIB3A_AE_SCENE_BEACH
    p->paraIdxForceOn[6] =1;    //LIB3A_AE_SCENE_CANDLELIGHT
    p->paraIdxForceOn[7] =1;    //LIB3A_AE_SCENE_FIREWORKS
    p->paraIdxForceOn[8] =1;    //LIB3A_AE_SCENE_LANDSCAPE
    p->paraIdxForceOn[9] =1;    //LIB3A_AE_SCENE_PORTRAIT
    p->paraIdxForceOn[10] =1;   //LIB3A_AE_SCENE_NIGHT_PORTRAIT
    p->paraIdxForceOn[11] =1;   //LIB3A_AE_SCENE_PARTY
    p->paraIdxForceOn[12] =1;   //LIB3A_AE_SCENE_SNOW
    p->paraIdxForceOn[13] =1;   //LIB3A_AE_SCENE_SPORTS
    p->paraIdxForceOn[14] =1;   //LIB3A_AE_SCENE_STEADYPHOTO
    p->paraIdxForceOn[15] =1;   //LIB3A_AE_SCENE_SUNSET
    p->paraIdxForceOn[16] =1;   //LIB3A_AE_SCENE_THEATRE
    p->paraIdxForceOn[17] =1;   //LIB3A_AE_SCENE_ISO_ANTI_SHAKE
    p->paraIdxForceOn[18] =1;   //LIB3A_AE_SCENE_BACKLIGHT

    p->paraIdxAuto[0] =1;  //default
    p->paraIdxAuto[1] =0;  //LIB3A_AE_SCENE_OFF
    p->paraIdxAuto[2] =0;  //LIB3A_AE_SCENE_AUTO
    p->paraIdxAuto[3] =1;  //LIB3A_AE_SCENE_NIGHT
    p->paraIdxAuto[4] =1;  //LIB3A_AE_SCENE_ACTION
    p->paraIdxAuto[5] =1;  //LIB3A_AE_SCENE_BEACH
    p->paraIdxAuto[6] =1;  //LIB3A_AE_SCENE_CANDLELIGHT
    p->paraIdxAuto[7] =1;  //LIB3A_AE_SCENE_FIREWORKS
    p->paraIdxAuto[8] =1;  //LIB3A_AE_SCENE_LANDSCAPE
    p->paraIdxAuto[9] =1;  //LIB3A_AE_SCENE_PORTRAIT
    p->paraIdxAuto[10] =1; //LIB3A_AE_SCENE_NIGHT_PORTRAIT
    p->paraIdxAuto[11] =1; //LIB3A_AE_SCENE_PARTY
    p->paraIdxAuto[12] =1; //LIB3A_AE_SCENE_SNOW
    p->paraIdxAuto[13] =1; //LIB3A_AE_SCENE_SPORTS
    p->paraIdxAuto[14] =1; //LIB3A_AE_SCENE_STEADYPHOTO
    p->paraIdxAuto[15] =1; //LIB3A_AE_SCENE_SUNSET
    p->paraIdxAuto[16] =1; //LIB3A_AE_SCENE_THEATRE
    p->paraIdxAuto[17] =1; //LIB3A_AE_SCENE_ISO_ANTI_SHAKE
    p->paraIdxAuto[18] =1; //LIB3A_AE_SCENE_BACKLIGHT



	//--------------------
	//eng level
	//index mode
	//torch
	p->engLevel.torchDuty = 1;
	//af
	p->engLevel.afDuty = 1;
	//pf, mf, normal
	p->engLevel.pfDuty = 1;
	p->engLevel.mfDutyMax = 15;
	p->engLevel.mfDutyMin = -1;
	//low bat
	p->engLevel.IChangeByVBatEn=1;
	p->engLevel.vBatL = 3600;	//mv
	p->engLevel.pfDutyL = 0;
	p->engLevel.mfDutyMaxL = 3;
	p->engLevel.mfDutyMinL = -1;
	//burst setting
	p->engLevel.IChangeByBurstEn=1;
	p->engLevel.pfDutyB = 0;
	p->engLevel.mfDutyMaxB = 3;
	p->engLevel.mfDutyMinB = -1;
	//high current setting
	p->engLevel.decSysIAtHighEn = 1;
	p->engLevel.dutyH = 15;

    //LT
	p->engLevelLT.torchDuty = -1;
	//af
	p->engLevelLT.afDuty = -1;
	//pf, mf, normal
	p->engLevelLT.pfDuty = -1;
	p->engLevelLT.mfDutyMax = 15;
	p->engLevelLT.mfDutyMin = -1;
	//low bat
	p->engLevelLT.pfDutyL = -1;
	p->engLevelLT.mfDutyMaxL = 3;
	p->engLevelLT.mfDutyMinL = -1;
	//burst setting
	p->engLevelLT.pfDutyB = -1;
	p->engLevelLT.mfDutyMaxB =3;
	p->engLevelLT.mfDutyMinB = -1;


        p->dualTuningPara.toleranceEV_pos = 30;
        p->dualTuningPara.toleranceEV_neg = 30;

        p->dualTuningPara.XYWeighting = 64;  //0.5  , 128 base
        p->dualTuningPara.useAwbPreferenceGain = 1;
        p->dualTuningPara.envOffsetIndex[0] = -150;
        p->dualTuningPara.envOffsetIndex[1] = -50;
        p->dualTuningPara.envOffsetIndex[2] = 0;
        p->dualTuningPara.envOffsetIndex[3] = 100;

        p->dualTuningPara.envXrOffsetValue[0] = 0;
        p->dualTuningPara.envXrOffsetValue[1] = 0;
        p->dualTuningPara.envXrOffsetValue[2] = 0;
        p->dualTuningPara.envXrOffsetValue[3] = 0;

        p->dualTuningPara.envYrOffsetValue[0] = 0;
        p->dualTuningPara.envYrOffsetValue[1] = 0;
        p->dualTuningPara.envYrOffsetValue[2] = 0;
        p->dualTuningPara.envYrOffsetValue[3] = 0;


	return 0;
}


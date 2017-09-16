#define LOG_TAG "PQ"

#include <cutils/xlog.h>
#include <stdint.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <cutils/properties.h>


#include "ddp_drv.h"
#include "cust_gamma.h"
#include "cust_color.h"
#include "cust_tdshp.h"


int drvID = -1;

static int getLcmIndexOfGamma(int dev)
{
    static int lcmIdx = -1;

    if (lcmIdx == -1) {
        int ret = ioctl(dev, DISP_IOCTL_GET_LCMINDEX, &lcmIdx);
        if (ret == 0) {
            if (lcmIdx < 0 || lcmIdx >= GAMMA_LCM_MAX) {
                XLOGE("Invalid LCM index %d, GAMMA_LCM_MAX = %d", lcmIdx, GAMMA_LCM_MAX);
                lcmIdx = 0;
            }
        } else {
            XLOGE("ioctl(DISP_IOCTL_GET_LCMINDEX) return %d", ret);
            lcmIdx = 0;
        }
    }

    XLOGI("LCM index: %d/%d", lcmIdx, GAMMA_LCM_MAX);

    return lcmIdx;
}


static void configGamma(int dev, int picMode)
{
#if (GAMMA_LCM_MAX > 0) && (GAMMA_INDEX_MAX > 0)
    int lcmIndex = 0;
    int gammaIndex = 0;
#endif

#if GAMMA_LCM_MAX > 1
    lcmIndex = getLcmIndexOfGamma(dev);
#endif

#if GAMMA_INDEX_MAX > 1
    // get gamma index from runtime property configuration
    char property[PROPERTY_VALUE_MAX];

    gammaIndex = GAMMA_INDEX_DEFAULT;
    if (picMode == PQ_PIC_MODE_USER_DEF &&
            property_get(GAMMA_INDEX_PROPERTY_NAME, property, NULL) > 0 &&
            strlen(property) > 0)
    {
        gammaIndex = atoi(property);
    }

    if (gammaIndex < 0 || GAMMA_INDEX_MAX <= gammaIndex)
        gammaIndex = GAMMA_INDEX_DEFAULT;

    XLOGI("Gamma index: %d/%d", gammaIndex, GAMMA_INDEX_MAX);
#endif

#if (GAMMA_LCM_MAX > 0) && (GAMMA_INDEX_MAX > 0)
    DISP_GAMMA_LUT_T *driverGamma = new DISP_GAMMA_LUT_T;

    const gamma_entry_t *entry = &(cust_gamma[lcmIndex][gammaIndex]);
    driverGamma->hw_id = DISP_GAMMA0;
    for (int i = 0; i < DISP_GAMMA_LUT_SIZE; i++) {
        driverGamma->lut[i] = GAMMA_ENTRY((*entry)[0][i], (*entry)[1][i], (*entry)[2][i]);
    }

    ioctl(dev, DISP_IOCTL_SET_GAMMALUT, driverGamma);

    delete driverGamma;
#endif
}

static void configCCorr(int dev_fd)
{    
    DISP_CCORR_COEF_T *corr = new DISP_CCORR_COEF_T;
 
    if (corr == NULL)
    {
        return;        
    }
    
    // init CCORR0 matrix
    corr->hw_id = DISP_CCORR0;
    memcpy(corr->coef, &g_ccorr_matrix[DISP_CCORR0], sizeof(corr->coef));
    ioctl(dev_fd, DISP_IOCTL_SET_CCORR, corr);

    // init CCORR1 matrix
    corr->hw_id = DISP_CCORR1;
    memcpy(corr->coef, &g_ccorr_matrix[DISP_CCORR1], sizeof(corr->coef));
    ioctl(dev_fd, DISP_IOCTL_SET_CCORR, corr);

    delete corr;
}

static void configPQ(int drvID, int i)
{
    char value[PROPERTY_VALUE_MAX];

    XLOGD("config PQ...");

    // pq index
    ioctl(drvID, DISP_IOCTL_SET_PQINDEX, &pqindex);

    if (i == PQ_PIC_MODE_STANDARD)
    {
        ioctl(drvID, DISP_IOCTL_SET_PQPARAM, &pqparam_standard);
        ioctl(drvID, DISP_IOCTL_SET_PQ_GAL_PARAM, &pqparam_standard);
        property_get(PQ_TDSHP_PROPERTY_STR, value, PQ_TDSHP_STANDARD_DEFAULT);
        property_set(PQ_TDSHP_PROPERTY_STR, value);
    }
    else if (i == PQ_PIC_MODE_VIVID)
    {
        ioctl(drvID, DISP_IOCTL_SET_PQPARAM, &pqparam_vivid);
        ioctl(drvID, DISP_IOCTL_SET_PQ_GAL_PARAM, &pqparam_vivid);
        property_get(PQ_TDSHP_PROPERTY_STR, value, PQ_TDSHP_VIVID_DEFAULT);
        property_set(PQ_TDSHP_PROPERTY_STR, value);
    }
    else if (i == PQ_PIC_MODE_USER_DEF)
    {
        DISP_PQ_PARAM pqparam;

        // base on vivid
        memcpy(&pqparam, &pqparam_vivid, sizeof(pqparam));

        property_get(PQ_TDSHP_PROPERTY_STR, value, PQ_TDSHP_USER_DEFAULT);
        i = atoi(value);
        XLOGD("[PQ][main pq] main, property get... tdshp[%d]", i);
        pqparam.u4SHPGain = i;

        property_get(PQ_GSAT_PROPERTY_STR, value, PQ_GSAT_INDEX_DEFAULT);
        i = atoi(value);
        XLOGD("[PQ][main pq] main, property get... gsat[%d]", i);
        pqparam.u4SatGain = i;

        property_get(PQ_CONTRAST_PROPERTY_STR, value, PQ_CONTRAST_INDEX_DEFAULT);
        i = atoi(value);
        XLOGD("[PQ][main pq] main, property get... contrash[%d]", i);
        pqparam.u4Contrast = i;

        property_get(PQ_PIC_BRIGHT_PROPERTY_STR, value, PQ_PIC_BRIGHT_INDEX_DEFAULT);
        i = atoi(value);
        XLOGD("[PQ][main pq] main, property get... pic bright[%d]", i);
        pqparam.u4Brightness = i;

        ioctl(drvID, DISP_IOCTL_SET_PQPARAM, &pqparam);
        ioctl(drvID, DISP_IOCTL_SET_PQ_GAL_PARAM, &pqparam);
    }
    else
    {
        XLOGE("[PQ][main pq] main, property get... unknown pic_mode[%d]", i);
        ioctl(drvID, DISP_IOCTL_SET_PQPARAM, &pqparam_standard);
        ioctl(drvID, DISP_IOCTL_SET_PQ_GAL_PARAM, &pqparam_standard);
    }

    ioctl(drvID, DISP_IOCTL_SET_PQ_CAM_PARAM, &pqparam_camera);

    ioctl(drvID, DISP_IOCTL_SET_TDSHPINDEX, &tdshpindex);

    XLOGD("config PQ... end");
}

int main(int argc, char** argv)
{
    int actionID=0, RegBase = 0, RegValue = 0, err = 0;
    char fileName[256];

    XLOGD("PQ init start...");
    if(drvID == -1) //initial
        drvID = open("/dev/mtk_disp_mgr", O_RDONLY, 0);

    if (drvID < 0)
    {
        XLOGE("PQ device open failed!!");
    }

    char value[PROPERTY_VALUE_MAX];
    property_get(PQ_PIC_MODE_PROPERTY_STR, value, PQ_PIC_MODE_DEFAULT);
    int picMode = atoi(value);

    XLOGD("[PQ][main pq] main, property get... pic_mode[%d]", picMode);

    configPQ(drvID, picMode);

    configGamma(drvID, picMode);
    
    configCCorr(drvID);

    XLOGD("PQ init end !");

    if (drvID > 0)
    {
        close(drvID);
    }

    return 0;
}

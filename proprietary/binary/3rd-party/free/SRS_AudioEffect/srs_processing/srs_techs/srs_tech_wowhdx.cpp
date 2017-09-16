/*======================================================================*
 DTS, Inc.
 5220 Las Virgenes Road
 Calabasas, CA 91302  USA

 CONFIDENTIAL: CONTAINS CONFIDENTIAL PROPRIETARY INFORMATION OWNED BY
 DTS, INC. AND/OR ITS AFFILIATES ("DTS"), INCLUDING BUT NOT LIMITED TO
 TRADE SECRETS, KNOW-HOW, TECHNICAL AND BUSINESS INFORMATION. USE,
 DISCLOSURE OR DISTRIBUTION OF THE SOFTWARE IN ANY FORM IS LIMITED TO
 SPECIFICALLY AUTHORIZED LICENSEES OF DTS.  ANY UNAUTHORIZED
 DISCLOSURE IS A VIOLATION OF STATE, FEDERAL, AND INTERNATIONAL LAWS.
 BOTH CIVIL AND CRIMINAL PENALTIES APPLY.

 DO NOT DUPLICATE. COPYRIGHT 2014, DTS, INC. ALL RIGHTS RESERVED.
 UNAUTHORIZED DUPLICATION IS A VIOLATION OF STATE, FEDERAL AND
 INTERNATIONAL LAWS.

 ALGORITHMS, DATA STRUCTURES AND METHODS CONTAINED IN THIS SOFTWARE
 MAY BE PROTECTED BY ONE OR MORE PATENTS OR PATENT APPLICATIONS.
 UNLESS OTHERWISE PROVIDED UNDER THE TERMS OF A FULLY-EXECUTED WRITTEN
 AGREEMENT BY AND BETWEEN THE RECIPIENT HEREOF AND DTS, THE FOLLOWING
 TERMS SHALL APPLY TO ANY USE OF THE SOFTWARE (THE "PRODUCT") AND, AS
 APPLICABLE, ANY RELATED DOCUMENTATION:  (i) ANY USE OF THE PRODUCT
 AND ANY RELATED DOCUMENTATION IS AT THE RECIPIENT’S SOLE RISK:
 (ii) THE PRODUCT AND ANY RELATED DOCUMENTATION ARE PROVIDED "AS IS"
 AND WITHOUT WARRANTY OF ANY KIND AND DTS EXPRESSLY DISCLAIMS ALL
 WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 PURPOSE, REGARDLESS OF WHETHER DTS KNOWS OR HAS REASON TO KNOW OF THE
 USER'S PARTICULAR NEEDS; (iii) DTS DOES NOT WARRANT THAT THE PRODUCT
 OR ANY RELATED DOCUMENTATION WILL MEET USER'S REQUIREMENTS, OR THAT
 DEFECTS IN THE PRODUCT OR ANY RELATED DOCUMENTATION WILL BE
 CORRECTED; (iv) DTS DOES NOT WARRANT THAT THE OPERATION OF ANY
 HARDWARE OR SOFTWARE ASSOCIATED WITH THIS DOCUMENT WILL BE
 UNINTERRUPTED OR ERROR-FREE; AND (v) UNDER NO CIRCUMSTANCES,
 INCLUDING NEGLIGENCE, SHALL DTS OR THE DIRECTORS, OFFICERS, EMPLOYEES,
 OR AGENTS OF DTS, BE LIABLE TO USER FOR ANY INCIDENTAL, INDIRECT,
 SPECIAL, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO
 DAMAGES FOR LOSS OF BUSINESS PROFITS, BUSINESS INTERRUPTION, AND LOSS
 OF BUSINESS INFORMATION) ARISING OUT OF THE USE, MISUSE, OR INABILITY
 TO USE THE PRODUCT OR ANY RELATED DOCUMENTATION.
*======================================================================*/

#include "srs_techs/srs_tech_headers.h"

#include SRSLIBINC(srs_wowhdx_api.h)
#include SRSLIBINC(srs_iir_design_api.h)
#include SRSLIBINC(srs_trubass_design_api.h)

#undef  LOG_TAG
#define LOG_TAG "SRS_Tech_WOWHDX"

namespace android {

struct SRS_Source_WOWHD {
    SRSWowhdxObj            Obj;
    void                    *pObjBuffer;
    void                    *pObjWorkspace;
    void                    *pStatBuffer;
    int                     SampleRate;
    int                     ChannelCount;
    SRSCoef32               TBCoefs[SRS_TBHD_FXP_COEFFICIENT_ARRAY_LEN];
    float                   TBWant, TBActive;
    float                   DFWant, DFActive;
    SRSWowhdxFilterConfig   FiltHold;
    SRSCoef16               XHPCoefs[32];
    SRSCoef16               XLPCoefs[32];
    SRSCoef16               TBHPCoefs[32];
    SRS_Tech_WOWHD          Active;                 // Cache of all current values for the API
    bool                    ForceActive;            // Ignore differences - force update of Active
    bool                    DidCreate;
    bool                    DidConfig;

    SRS_Source_WOWHD();
    ~SRS_Source_WOWHD();

    void Create(SRS_Source_Out *pOut);
    void Config(SRS_Source_Out *pOut, SRS_Tech_WOWHD *pCFG, bool bBypass);
    void Process_256(SRSSamp *pIn, SRSSamp *pOut);
};

SRS_Source_WOWHD::SRS_Source_WOWHD(){
    SRS_LOG("WOWHDX Created");

    SRSWowhdxCreateData filterMaxOrder = { 8  };

    DidCreate = false;
    DidConfig = false;

    pObjBuffer = malloc(SRS_Wowhdx_GetObjSize(&filterMaxOrder));
    pObjWorkspace = malloc(SRS_WOWHDX_WORKSPACE_SIZE);

#ifdef _SRSCFG_PERFTRACK
    SRS_LOG("[SRS_RAMSIZE] WOWHDX: %d", SRS_Wowhdx_GetObjSize() + SRS_WOWHDX_WORKSPACE_SIZE + sizeof(SRS_Source_WOWHD));
#endif  //_SRSCFG_PERFTRACK
}

SRS_Source_WOWHD::~SRS_Source_WOWHD(){
    if (pObjWorkspace != NULL) free(pObjWorkspace);
    pObjWorkspace = NULL;

    if (pObjBuffer != NULL) free(pObjBuffer);
    pObjBuffer = NULL;

    DidCreate = false;
    DidConfig = false;

    SRS_LOG("WOWHDX Destroyed");
}

void SRS_Source_WOWHD::Create(SRS_Source_Out *pOut){
    if (pOut->SampleRate <= 0) return;
    if (pOut->ChannelCount != 2) return;

    SampleRate = pOut->SampleRate;
    ChannelCount = pOut->ChannelCount;

    DidCreate = true;
    ForceActive = true;

    //WOW HDX needs to do 64*2 (=128) points FFT internally. The FFT lib must be
    //initilized for this length firt of all.
    //If another IP uses the same length FFT, the initialization can be shared, i.e, one FFT length only need to be initialized once application widely.
    void *fftlibBuf;
    fftlibBuf = malloc(SRS_Fft_GetPlanBufSize(64*2));
    SRS_Fft_CreatePlan(fftlibBuf, 64*2, SRS_RFFT_32C16);

    SRSWowhdxCreateData filterMaxOrder = { 8  };
    SRS_Wowhdx_CreateObj(&Obj, pObjBuffer, NULL, NULL, &filterMaxOrder);

    TBWant = TBActive = -1.0f;
    DFWant = DFActive = -1.0f;
}

void SRS_Source_WOWHD::Config(SRS_Source_Out *pOut, SRS_Tech_WOWHD *pCFG, bool bBypass){
    if (DidCreate == false) return;

    bool bDiff_Filts = ForceActive;
    if (bDiff_Filts == false){
        if (DIFF_FORCED(XOverOrder) || DIFF_FORCED(XOverFreq)) bDiff_Filts = true;
        else if (DIFF_FORCED(HPFOrder) || DIFF_FORCED(HPFFreq)) bDiff_Filts = true;
    }

    if (bDiff_Filts){
        DidConfig = false;  // Force a re init...
        ForceActive = true;

        char tWork[SRS_IIR_DESIGN_WORKSPACE_SIZE(8)];
        int tXOverDelta = pCFG->XOverFreq/(2*pCFG->XOverOrder-1);
        SRS_IirFilterDesignFxp16(pCFG->XOverFreq - tXOverDelta, pCFG->XOverOrder, SampleRate, SRS_IIR_DESIGN_LP, XLPCoefs, tWork);
        SRS_IirFilterDesignFxp16(pCFG->XOverFreq + tXOverDelta, pCFG->XOverOrder, SampleRate, SRS_IIR_DESIGN_HP, XHPCoefs, tWork);
        SRS_IirFilterDesignFxp16(pCFG->HPFFreq, pCFG->HPFOrder, SampleRate, SRS_IIR_DESIGN_HP, TBHPCoefs, tWork);

        //FiltHold.XoverOrder = pCFG->XOverOrder;// fixed to 4
        FiltHold.XoverLpfCoefs = XLPCoefs;
        FiltHold.XoverHpfCoefs = XHPCoefs;

        FiltHold.HpfOrder = pCFG->HPFOrder;
        FiltHold.HpfCoefs = TBHPCoefs;
    }

    if (DidConfig == false){    // Re-init!
        // Initialise the objects
        if (SampleRate < 33000)         SRS_Wowhdx_InitObj32k(Obj, &FiltHold);
        else if (SampleRate < 42000)    SRS_Wowhdx_InitObj44k(Obj, &FiltHold);
        else                            SRS_Wowhdx_InitObj48k(Obj, &FiltHold);
        SRS_Wowhdx_SetControlDefaults(Obj);
        ForceActive = true;
    }

    if (DIFF_FORCED(IGain))     SRS_Wowhdx_SetInputGain(Obj, SRS_FXP16(Tool_MaxZero(pCFG->IGain, 1.0f), SRS_WOWHDX_GAIN_IWL));
    if (DIFF_FORCED(OGain))     SRS_Wowhdx_SetOutputGain(Obj, SRS_FXP16(Tool_MaxZero(pCFG->OGain, 1.0f), SRS_WOWHDX_GAIN_IWL));
    if (DIFF_FORCED(DoHPF))     SRS_Wowhdx_SetHpfEnable(Obj, pCFG->DoHPF);
    if (DIFF_FORCED(DoXOver))   SRS_Wowhdx_SetCrossoverEnable(Obj, pCFG->DoXOver);
    if (DIFF_FORCED(DoTB))      SRS_Wowhdx_SetTBHDEnable(Obj, pCFG->DoTB);

    // Set Trubass controls
    if (DIFF_FORCED(TBMin) || DIFF_FORCED(TBWindow) || DIFF_FORCED(TBSlide)){
        TBWant = Tool_MaxZero(pCFG->TBMin+(pCFG->TBWindow*pCFG->TBSlide), 1.0f);
        //SRS_Wowhdx_SetTBHDControls(Obj, SRS_FXP16(TBWant, SRS_SATB_CTRL_IWL));
        if (ForceActive) TBActive = -1.0f;
    }

    if (DIFF_FORCED(TBCompress))SRS_Wowhdx_SetTBHDCompressorLevel(Obj, SRS_FXP16(Tool_MaxZero(pCFG->TBCompress, 1.0f), SRS_TBHD_CTRL_IWL));
    if (DIFF_FORCED(TBMode)){
        if (pCFG->TBMode == 0)  SRS_Wowhdx_SetTBHDMode(Obj, SRS_TBHD_MODE_MONO);
        else                    SRS_Wowhdx_SetTBHDMode(Obj, SRS_TBHD_MODE_STEREO);
    }

    if (DIFF_FORCED(TBSize) || DIFF_FORCED(TBAnalyze)){
        char tWork[SRS_TBHD_DESIGN_WORKSPACE_SIZE];
        SRS_TruBassHDFilterDesignFxp32(pCFG->TBSize, pCFG->TBAnalyze, SampleRate, TBCoefs, tWork);

        SRSTBHDSpeakerSize tSetCustom;
        tSetCustom.AudioFilter = SRS_TBHD_SPEAKER_LF_RESPONSE_CUSTOM;
        tSetCustom.AnalysisFilter = SRS_TBHD_SPEAKER_LF_RESPONSE_CUSTOM;

        SRS_Wowhdx_SetTBHDSpeakerSize(Obj, tSetCustom);
        SRS_Wowhdx_SetTBHDCustomSpeakerCoefs(Obj, TBCoefs);
    }

    if (DIFF_FORCED(DoTBSplit)) SRS_Wowhdx_SetTBHDSplitAnalysisEnable(Obj, pCFG->DoTBSplit);

    // Set SRS3D controls
    //if (DIFF_FORCED(DoSRS))     SRS_Wowhdx_SetEnhanceMode(Obj, (pCFG->DoSRS == true) ? SRS_WOWHDX_STEREO_ENHANCE_SRS3D : SRS_WOWHDX_STEREO_ENHANCE_NONE);
    if (DIFF_FORCED(Space))     SRS_Wowhdx_SetSRS3DSpaceLevel(Obj, SRS_FXP16(pCFG->Space, SRS_SRS3D_CTRL_IWL));
    if (DIFF_FORCED(Center))    SRS_Wowhdx_SetSRS3DCenterLevel(Obj, SRS_FXP16(pCFG->Center, SRS_SRS3D_CTRL_IWL));

    if (DIFF_FORCED(SRSMode)){
        if (pCFG->SRSMode == 0)         SRS_Wowhdx_SetSRS3DMode(Obj, SRS_SRS3D_MONO);
        else if (pCFG->SRSMode == 1)    SRS_Wowhdx_SetSRS3DMode(Obj, SRS_SRS3D_SINGLE_SPEAKER);
        else                            SRS_Wowhdx_SetSRS3DMode(Obj, SRS_SRS3D_STEREO);
    }

    if (DIFF_FORCED(SRSType)){
        if (pCFG->SRSType == 0) SRS_Wowhdx_SetSRS3DHeadphoneEnable(Obj, 0); // SRSType == 0 - Speaker...
        else                    SRS_Wowhdx_SetSRS3DHeadphoneEnable(Obj, 1); // SRSType == 1 - Headphones...
    }

    // Set Focus controls
    if (DIFF_FORCED(DoFocus))   SRS_Wowhdx_SetFocusEnable(Obj, pCFG->DoFocus);
    if (DIFF_FORCED(Focus))     SRS_Wowhdx_SetFocusLevel(Obj, SRS_FXP16(Tool_MaxZero(pCFG->Focus, 1.0f), SRS_FOCUS_IWL));

    // Set Definition controls
    if (DIFF_FORCED(DoDef))     SRS_Wowhdx_SetDefEnable(Obj, pCFG->DoDef);
    if (DIFF_FORCED(DefMin) || DIFF_FORCED(DefWindow) || DIFF_FORCED(DefSlide)){
        DFWant = Tool_MaxZero(pCFG->DefMin+(pCFG->DefWindow*pCFG->DefSlide), 1.0f);
        //SRS_Wowhdx_SetDefLevel(Obj, SRS_FXP16(DFWant, SRS_DEFINITION_IWL));
        if (ForceActive) DFActive = -1.0f;
    }

    //if (DIFF_FORCED(DoWS))      SRS_Wowhdx_SetEnhanceMode(Obj, (pCFG->DoWS == true) ? SRS_WOWHDX_STEREO_ENHANCE_WDSRD : SRS_WOWHDX_STEREO_ENHANCE_NONE);
    //if (DIFF_FORCED(WSIGain))   SRS_SetWowhdPlusWdSrdInputGain(Obj, SRS_FXP16(Tool_MaxZero(pCFG->WSIGain, 1.0f), SRS_WIDESRD_GAIN_IWL));
    if (DIFF_FORCED(WSCGain))   SRS_Wowhdx_SetWdSrdCenterBoostGain(Obj, SRS_FXP16(Tool_MaxZero(pCFG->WSCGain, 1.0f), SRS_WIDESRD_GAIN_IWL));
    if (DIFF_FORCED(WSSpkSep))  SRS_Wowhdx_SetWdSrdSpeakerSeparationLevel(Obj, SRS_FXP16(pCFG->WSSpkSep, SRS_WIDESRD_SPK_SEP_IWL));

    if (DIFF_FORCED(DoSRS) || DIFF_FORCED(DoWS))
    {
        SRS_Wowhdx_SetEnhanceMode(Obj, (pCFG->DoWS == true) ? SRS_WOWHDX_STEREO_ENHANCE_WDSRD : ((pCFG->DoSRS == true) ? SRS_WOWHDX_STEREO_ENHANCE_SRS3D : SRS_WOWHDX_STEREO_ENHANCE_NONE));
    }


    // WOWHDx enable
    if (bBypass)
        SRS_Wowhdx_SetEnable(Obj, 0);
    else
        SRS_Wowhdx_SetEnable(Obj, 1);

    DidConfig = true;
    Active = *pCFG;
    ForceActive = false;
}

void SRS_Source_WOWHD::Process_256(SRSSamp *pIn, SRSSamp *pOut){
    if (TBWant != TBActive){    // Slide into requested TB value?
        if (TBActive == -1.0f) TBActive = TBWant;
        else Tool_SeekValue(TBWant, TBActive);

        SRS_Wowhdx_SetTBHDLevel(Obj, SRS_FXP16(TBActive, SRS_TBHD_CTRL_IWL));
    }

    if (DFWant != DFActive){    // Slide into requested Def value?
        if (DFActive == -1.0f) DFActive = DFWant;
        else Tool_SeekValue(DFWant, DFActive);

        SRS_Wowhdx_SetDefLevel(Obj, SRS_FXP16(DFActive, SRS_DEF_IWL));
    }

    int i;

    SRSStereoCh inChans;
    inChans.Left = pIn;
    inChans.Right = pIn+256;

    for (i=0; i<4; i++){
        SRS_Wowhdx_Process(Obj, &inChans, pObjWorkspace);
        inChans.Left += 64;
        inChans.Right += 64;
    }

    memcpy(pOut, pIn, 512*sizeof(SRSSamp));
}

// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
// =-=-=-=-=-=- External Interfacing =-=-=-=-=-=-
// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

SRS_Source_WOWHD* SRS_Create_WOWHD(SRS_Source_Out* pOut){
    SRS_Source_WOWHD* pSrc = new SRS_Source_WOWHD();
    pSrc->Create(pOut);
    return pSrc;
}

void SRS_Destroy_WOWHD(SRS_Source_WOWHD* pSrc, SRS_Source_Out* pOut){
    if (pSrc == NULL) return;
    delete pSrc;
}

void SRS_Config_WOWHD(SRS_Source_WOWHD* pSrc, SRS_Source_Out* pOut, SRS_Tech_WOWHD* pCFG, bool bBypass){
    if (pSrc == NULL) return;
    pSrc->Config(pOut, pCFG, bBypass);
}

void SRS_Process_WOWHD_256(SRS_Source_WOWHD *pSrc, SRSSamp *pIn, SRSSamp *pOut){
    if (pSrc == NULL) return;
    if (pSrc->DidConfig == false) return;

    pSrc->Process_256(pIn, pOut);
}

enum {
    WHD_IGain,
    WHD_OGain,

    WHD_DoTB,
    WHD_TBMin,
    WHD_TBWindow,
    WHD_TBSlide,
    WHD_TBSlideUD,
    WHD_TBCompress,
    WHD_TBFreq,
    WHD_TBAnalyze,
    WHD_TBDoSplit,
    WHD_TBMode,

    WHD_DoXOver,
    WHD_XOverOrder,
    WHD_XOverFreq,

    WHD_DoHPF,
    WHD_HPFOrder,
    WHD_HPFFreq,

    WHD_DoSRS,
    WHD_Space,
    WHD_Center,
    WHD_SRSMode,
    WHD_SRSType,

    WHD_DoFocus,
    WHD_Focus,

    WHD_DoDef,
    WHD_DefMin,
    WHD_DefWindow,
    WHD_DefSlide,
    WHD_DefSlideUD,

#ifdef _SRSCFG_TRUMEDIA_HD
    WHD_DoWS,
    WHD_WSIGain,
    WHD_WSCGain,
    WHD_WSSpkSep,
#endif  // _SRSCFG_TRUMEDIA_HD

    WHD_Skip,
};

SRS_Param gWOWHD_Params[] = {
    { WHD_IGain,        SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.6f,   0.0f,   1.0f,       "wowhd_igain", "Input Gain", "gain", "", 0},
    { WHD_OGain,        SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     1.0f,   0.0f,   1.0f,       "wowhd_ogain", "Output Gain", "gain", "", 0},

    { WHD_DoTB,         SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   1.0f,       "wowhd_trubass_enable", "TruBass Toggle", "toggle", "", 0},
    { WHD_TBMin,        SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.0f,   0.0f,   1.0f,       "wowhd_trubass_min", "TruBass Min", "control", "", 0},
    { WHD_TBWindow,     SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     1.0f,   0.0f,   1.0f,       "wowhd_trubass_window", "TruBass Window", "control", "", 0},
    { WHD_TBSlide,      SRS_PTYP_PREF,  SRS_PFMT_FLOAT,     0.6f,   0.0f,   1.0f,       "wowhd_trubass_slide", "TruBass Slider", "control", "", 0},
    { WHD_TBSlideUD,    SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.6f,   0.0f,   1.0f,       "wowhd_trubass_slide_udef", "TruBass Slider - User Default", "control", "", 0},
    { WHD_TBCompress,   SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.25f,  0.0f,   1.0f,       "wowhd_trubass_compressor", "TruBass Compressor", "control", "", 0},
    { WHD_TBFreq,       SRS_PTYP_ALIAS, SRS_PFMT_INT,       400.0f, 40.0f,  1200.0f,    "wowhd_trubass_size", "TruBass Speaker Size", "frequency", "", 0},
    { WHD_TBFreq,       SRS_PTYP_CFG,   SRS_PFMT_INT,       400.0f, 40.0f,  1200.0f,    "wowhd_trubass_freq", "TruBass Frequency", "frequency", "", 0},
    { WHD_TBAnalyze,    SRS_PTYP_CFG,   SRS_PFMT_INT,       200.0f, 40.0f,  1200.0f,    "wowhd_trubass_analysis", "TruBass Analysis Frequency", "frequency", "", 0},
    { WHD_TBDoSplit,    SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   0.0f,       "wowhd_trubass_sa_enable", "TruBass Split Analysis Toggle", "toggle", "", 0},
    { WHD_TBMode,       SRS_PTYP_CFG,   SRS_PFMT_ENUM,      0.0f,   0.0f,   0.0f,       "wowhd_trubass_mode", "TruBass Mode", "enum", "Mono,Stereo", 0},

    { WHD_DoXOver,      SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   1.0f,       "wowhd_xover_enable", "XOver Filter Toggle", "toggle", "", 0},
    { WHD_XOverOrder,   SRS_PTYP_CFG,   SRS_PFMT_INT,       6.0f,   1.0f,   6.0f,       "wowhd_xover_order", "XOver Filters Order", "", "", 0},
    { WHD_XOverFreq,    SRS_PTYP_CFG,   SRS_PFMT_INT,       200.0f, 40.0f,  1200.0f,    "wowhd_xover_freq", "XOver Filter Frequency", "frequency", "", 0},

    { WHD_DoHPF,        SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   1.0f,       "wowhd_hpf_enable", "HiPass Filter Toggle", "toggle", "", 0},
    { WHD_HPFOrder,     SRS_PTYP_CFG,   SRS_PFMT_INT,       6.0f,   1.0f,   6.0f,       "wowhd_hpf_order", "HiPass Filters Order", "", "", 0},
    { WHD_HPFFreq,      SRS_PTYP_CFG,   SRS_PFMT_INT,       200.0f, 40.0f,  1200.0f,    "wowhd_hpf_freq", "HiPass Filter Frequency", "frequency", "", 0},

    { WHD_DoSRS,        SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   1.0f,       "wowhd_srs_enable", "SRS3D Toggle", "toggle", "", 0},
    { WHD_Space,        SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.9f,   0.0f,   1.0f,       "wowhd_srs_space", "SRS3D Space", "control", "", 0},
    { WHD_Center,       SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.5f,   0.0f,   1.0f,       "wowhd_srs_center", "SRS3D Center", "control", "", 0},
    { WHD_SRSMode,      SRS_PTYP_CFG,   SRS_PFMT_ENUM,      2.0f,   0.0f,   0.0f,       "wowhd_srs_mode", "SRS3D Mode", "enum", "Mono,Single Speaker,Stereo", 0},
    { WHD_SRSType,      SRS_PTYP_CFG,   SRS_PFMT_ENUM,      0.0f,   0.0f,   0.0f,       "wowhd_srs_speaker", "SRS3D Speaker Type", "enum", "Speaker,Headphone", 0},

    { WHD_DoFocus,      SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   1.0f,       "wowhd_focus_enable", "Focus Toggle", "toggle", "", 0},
    { WHD_Focus,        SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.4f,   0.0f,   1.0f,       "wowhd_focus", "Focus", "control", "", 0},

    { WHD_DoDef,        SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   1.0f,       "wowhd_definition_enable", "Definition Toggle", "toggle", "", 0},
    { WHD_DefMin,       SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.0f,   0.0f,   1.0f,       "wowhd_definition_min", "Definition Min", "control", "", 0},
    { WHD_DefWindow,    SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     1.0f,   0.0f,   1.0f,       "wowhd_definition_window", "Definition Window", "control", "", 0},
    { WHD_DefSlide,     SRS_PTYP_PREF,  SRS_PFMT_FLOAT,     0.43f,  0.0f,   1.0f,       "wowhd_definition_slide", "Definition Slide", "control", "", 0},
    { WHD_DefSlideUD,   SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.43f,  0.0f,   1.0f,       "wowhd_definition_slide_udef", "Definition Slide - User Default", "control", "", 0},

#ifdef _SRSCFG_TRUMEDIA_HD
    { WHD_DoWS,         SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   1.0f,       "wowhd_widesur_enable", "WideSurround Toggle", "toggle", "", 0},
    { WHD_WSIGain,      SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     1.0f,   0.0f,   1.0f,       "wowhd_widesur_igain", "WideSurround Input Gain", "gain", "", 0},
    { WHD_WSCGain,      SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.6f,   0.0f,   1.0f,       "wowhd_widesur_centerboost", "WideSurround Center Boost", "gain", "", 0},
    { WHD_WSSpkSep,     SRS_PTYP_CFG,   SRS_PFMT_FLOAT,     0.0f,   0.0f,   1.0f,       "wowhd_widesur_separation", "WideSurround Speaker Separation", "control", "", 0},
#endif  // _SRSCFG_TRUMEDIA_HD

    { WHD_Skip,         SRS_PTYP_CFG,   SRS_PFMT_BOOL,      1.0f,   0.0f,   1.0f,       "wowhd_skip", "Skips WOWHD when true", "toggle", "", 0},
};

SRS_Param* SRS_GetBank_WOWHD(int& paramCount){
    paramCount = sizeof(gWOWHD_Params)/sizeof(SRS_Param);
    return gWOWHD_Params;
}

char* SRS_GetVersion_WOWHD(char* pWork, size_t maxBytes){
    int v1 = SRS_Wowhdx_GetTechVersion(SRS_VERSION_MAJOR);
    int v2 = SRS_Wowhdx_GetTechVersion(SRS_VERSION_MINOR);
    int v3 = SRS_Wowhdx_GetTechVersion(SRS_VERSION_REVISION);
    int v4 = SRS_Wowhdx_GetTechVersion(SRS_VERSION_RELEASE);
    int l1 = SRS_Wowhdx_GetLibVersion(SRS_VERSION_MAJOR);
    int l2 = SRS_Wowhdx_GetLibVersion(SRS_VERSION_MINOR);
    int l3 = SRS_Wowhdx_GetLibVersion(SRS_VERSION_REVISION);
    int l4 = SRS_Wowhdx_GetLibVersion(SRS_VERSION_RELEASE);
    snprintf(pWork, maxBytes, "%d.%d.%d.%d - lib %d.%d.%d.%d", v1, v2, v3, v4, l1, l2, l3, l4);
    return pWork;
}

void SRS_SetParam_WOWHD(SRS_Tech_WOWHD* pCFG, SRS_Param* pParam, const char* pValue){
    HELP_ParamIn In;

    switch (pParam->EnumID){
        case WHD_IGain: pCFG->IGain = In.GetFloat(pValue); break;
        case WHD_OGain: pCFG->OGain = In.GetFloat(pValue); break;

        case WHD_DoTB: pCFG->DoTB = In.GetBool(pValue); break;
        case WHD_TBMin: pCFG->TBMin = In.GetFloat(pValue); break;
        case WHD_TBWindow: pCFG->TBWindow = In.GetFloat(pValue); break;
        case WHD_TBSlide: pCFG->TBSlide = In.GetFloat(pValue); break;
        case WHD_TBSlideUD: pCFG->TBSlideUDef = In.GetFloat(pValue); break;
        case WHD_TBCompress: pCFG->TBCompress = In.GetFloat(pValue); break;
        case WHD_TBFreq: pCFG->TBSize = In.GetInt(pValue); break;
        case WHD_TBAnalyze: pCFG->TBAnalyze = In.GetInt(pValue); break;
        case WHD_TBDoSplit: pCFG->DoTBSplit = In.GetBool(pValue); break;
        case WHD_TBMode: pCFG->TBMode = In.GetInt(pValue); break;

        case WHD_DoXOver: pCFG->DoXOver = In.GetBool(pValue); break;
        case WHD_XOverOrder: pCFG->XOverOrder = In.GetInt(pValue); break;
        case WHD_XOverFreq: pCFG->XOverFreq = In.GetInt(pValue); break;

        case WHD_DoHPF: pCFG->DoHPF = In.GetBool(pValue); break;
        case WHD_HPFOrder: pCFG->HPFOrder = In.GetInt(pValue); break;
        case WHD_HPFFreq: pCFG->HPFFreq = In.GetInt(pValue); break;

        case WHD_DoSRS: pCFG->DoSRS = In.GetBool(pValue); break;
        case WHD_Space: pCFG->Space = In.GetFloat(pValue); break;
        case WHD_Center: pCFG->Center = In.GetFloat(pValue); break;
        case WHD_SRSMode: pCFG->SRSMode = In.GetInt(pValue); break;
        case WHD_SRSType: pCFG->SRSType = In.GetInt(pValue); break;

        case WHD_DoFocus: pCFG->DoFocus = In.GetBool(pValue); break;
        case WHD_Focus: pCFG->Focus = In.GetFloat(pValue); break;

        case WHD_DoDef: pCFG->DoDef = In.GetBool(pValue); break;
        case WHD_DefMin: pCFG->DefMin = In.GetFloat(pValue); break;
        case WHD_DefWindow: pCFG->DefWindow = In.GetFloat(pValue); break;
        case WHD_DefSlide: pCFG->DefSlide = In.GetFloat(pValue); break;
        case WHD_DefSlideUD: pCFG->DefSlideUDef = In.GetFloat(pValue); break;

#ifdef _SRSCFG_TRUMEDIA_HD
        case WHD_DoWS: pCFG->DoWS = In.GetBool(pValue); break;
        case WHD_WSIGain: pCFG->WSIGain = In.GetFloat(pValue); break;
        case WHD_WSCGain: pCFG->WSCGain = In.GetFloat(pValue); break;
        case WHD_WSSpkSep: pCFG->WSSpkSep = In.GetFloat(pValue); break;
#endif  // _SRSCFG_TRUMEDIA_HD

        case WHD_Skip: pCFG->Skip = In.GetBool(pValue); break;
    }
}

const char* SRS_GetParam_WOWHD(SRS_Tech_WOWHD* pCFG, SRS_Param* pParam){
    HELP_ParamOut Out;

    switch (pParam->EnumID){
        case WHD_IGain: return Out.FromFloat(pCFG->IGain);
        case WHD_OGain: return Out.FromFloat(pCFG->OGain);

        case WHD_DoTB: return Out.FromBool(pCFG->DoTB);
        case WHD_TBMin: return Out.FromFloat(pCFG->TBMin);
        case WHD_TBWindow: return Out.FromFloat(pCFG->TBWindow);
        case WHD_TBSlide: return Out.FromFloat(pCFG->TBSlide);
        case WHD_TBSlideUD: return Out.FromFloat(pCFG->TBSlideUDef);
        case WHD_TBCompress: return Out.FromFloat(pCFG->TBCompress);
        case WHD_TBFreq: return Out.FromInt(pCFG->TBSize);
        case WHD_TBAnalyze: return Out.FromInt(pCFG->TBAnalyze);
        case WHD_TBDoSplit: return Out.FromBool(pCFG->DoTBSplit);
        case WHD_TBMode: return Out.FromInt(pCFG->TBMode);

        case WHD_DoXOver: return Out.FromBool(pCFG->DoXOver);
        case WHD_XOverOrder: return Out.FromInt(pCFG->XOverOrder);
        case WHD_XOverFreq: return Out.FromInt(pCFG->XOverFreq);

        case WHD_DoHPF: return Out.FromBool(pCFG->DoHPF);
        case WHD_HPFOrder: return Out.FromInt(pCFG->HPFOrder);
        case WHD_HPFFreq: return Out.FromInt(pCFG->HPFFreq);

        case WHD_DoSRS: return Out.FromBool(pCFG->DoSRS);
        case WHD_Space: return Out.FromFloat(pCFG->Space);
        case WHD_Center: return Out.FromFloat(pCFG->Center);
        case WHD_SRSMode: return Out.FromInt(pCFG->SRSMode);
        case WHD_SRSType: return Out.FromInt(pCFG->SRSType);

        case WHD_DoFocus: return Out.FromBool(pCFG->DoFocus);
        case WHD_Focus: return Out.FromFloat(pCFG->Focus);

        case WHD_DoDef: return Out.FromBool(pCFG->DoDef);
        case WHD_DefMin: return Out.FromFloat(pCFG->DefMin);
        case WHD_DefWindow: return Out.FromFloat(pCFG->DefWindow);
        case WHD_DefSlide: return Out.FromFloat(pCFG->DefSlide);
        case WHD_DefSlideUD: return Out.FromFloat(pCFG->DefSlideUDef);

#ifdef _SRSCFG_TRUMEDIA_HD
        case WHD_DoWS: return Out.FromBool(pCFG->DoWS);
        case WHD_WSIGain: return Out.FromFloat(pCFG->WSIGain);
        case WHD_WSCGain: return Out.FromFloat(pCFG->WSCGain);
        case WHD_WSSpkSep: return Out.FromFloat(pCFG->WSSpkSep);
#endif  // _SRSCFG_TRUMEDIA_HD

        case WHD_Skip: return Out.FromBool(pCFG->Skip);
    }
    return "";
}

extern void SRS_Default_WOWHD(SRS_Tech_WOWHD* pCFG, const char* pBankName){

    pCFG->IGain = 0.5f;
    pCFG->OGain = 1.0f;

    pCFG->DoTB = true;
    pCFG->TBMin = 0.0f;
    pCFG->TBWindow = 1.0f;
    pCFG->TBSlideUDef = pCFG->TBSlide = 0.5f;
    pCFG->TBCompress = 0.20f;
    pCFG->TBSize = 400;
    pCFG->TBAnalyze = 200;
    pCFG->DoTBSplit = true;
    pCFG->TBMode = 1;

    pCFG->DoXOver = true;
    pCFG->XOverOrder = 4;
    pCFG->XOverFreq = 700;

    pCFG->DoHPF = true;
    pCFG->HPFOrder = 4;
    pCFG->HPFFreq = 200;

    pCFG->DoSRS = true;
    pCFG->SRSMode = 2;
    pCFG->SRSType = 0;
    pCFG->Space = 0.7f;
    pCFG->Center = 0.5f;

    pCFG->DoFocus = true;
    pCFG->Focus = 0.4f;

    pCFG->DoDef = true;
    pCFG->DefMin = 0.0f;
    pCFG->DefWindow = 0.5f;
    pCFG->DefSlideUDef = pCFG->DefSlide = 0.3f;

    pCFG->DoWS = false;
    pCFG->WSIGain = 1.0f;
    pCFG->WSCGain = 0.5f;
    pCFG->WSSpkSep = 0.5f;

    pCFG->Skip = false;

    if (pBankName != NULL){
        if (strcmp(pBankName, "srs_mus_int") == 0){
            pCFG->DefWindow = 0.6f;
        }

        if (strcmp(pBankName, "srs_mov_int") == 0){
            pCFG->TBSlideUDef = pCFG->TBSlide = 0.6f;
            pCFG->DoSRS = false;
            pCFG->SRSMode = 2;
            pCFG->Space = 0.8f;
            pCFG->DoWS = true;
            pCFG->WSCGain = 0.6f;
            pCFG->WSSpkSep = 0.1f;
        }

        if (strcmp(pBankName, "srs_pod_int") == 0){
            pCFG->TBSlideUDef = pCFG->TBSlide = 0.4f;
            pCFG->Space = 0.6f;
        }

        if (strcmp(pBankName, "srs_mus_ext") == 0){
            pCFG->TBSlideUDef = pCFG->TBSlide = 0.4f;
            pCFG->TBSize = 80;
            pCFG->TBAnalyze = 60;
            pCFG->XOverFreq = 300;
            pCFG->DoHPF = false;
            pCFG->HPFFreq = 20;
            pCFG->Space = 0.8f;
            pCFG->DefWindow = 0.6f;
        }

        if (strcmp(pBankName, "srs_pod_ext") == 0){
            pCFG->TBSlideUDef = pCFG->TBSlide = 0.4f;
            pCFG->TBSize = 110;
            pCFG->TBAnalyze = 80;
            pCFG->XOverFreq = 300;
            pCFG->DoHPF = false;
            pCFG->HPFFreq = 20;
            pCFG->Space = 0.8f;
            pCFG->DefWindow = 0.5f;
        }
    }
}

void SRS_UserDefault_WOWHD(SRS_Tech_WOWHD* pCFG){
    pCFG->TBSlide = pCFG->TBSlideUDef;
    pCFG->DefSlide = pCFG->DefSlideUDef;
}

};


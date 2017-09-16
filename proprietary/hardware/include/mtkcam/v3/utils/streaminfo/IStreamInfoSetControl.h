/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#ifndef _MTK_HARDWARE_INCLUDE_MTKCAM_V3_UTILS_STREAMINFO_ISTREAMINFOSETCONTROL_H_
#define _MTK_HARDWARE_INCLUDE_MTKCAM_V3_UTILS_STREAMINFO_ISTREAMINFOSETCONTROL_H_
//
#include <utils/KeyedVector.h>
#include <mtkcam/v3/stream/IStreamInfo.h>


/******************************************************************************
 *
 ******************************************************************************/
namespace NSCam {
namespace v3 {
namespace Utils {


/**
 * An interface of stream info set control.
 */
class IStreamInfoSetControl
    : public IStreamInfoSet
{
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  IStreamInfoSetControl Interface.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////                            Definitions.

    template <class IStreamInfoT>
    struct Map : public android::KeyedVector<StreamId_T, android::sp<IStreamInfoT> >
    {
    public:     ////                        Definitions.
        typedef android::KeyedVector<StreamId_T, android::sp<IStreamInfoT> >
                                                ParentT;
        typedef typename ParentT::key_type      key_type;
        typedef typename ParentT::value_type    value_type;

    public:     ////                        Operations.
        ssize_t                             addStream(value_type const& p)
                                            {
                                                return ParentT::add(p->getStreamId(), p);
                                            }
    };

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  Implementations.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
protected:  ////                            Definitions.

    template <class IStreamInfoT>
    struct Set
    {
        typedef Map<IStreamInfoT>           MapT;
        MapT                                mApp;
        MapT                                mHal;

        size_t                              size() const
                                            {
                                                return mApp.size() + mHal.size();
                                            }

        typename MapT::value_type           valueFor(StreamId_T id) const
                                            {
                                                ssize_t index = 0;
                                                if  ( 0 <= (index = mApp.indexOfKey(id)) ) return mApp.valueAt(index);
                                                if  ( 0 <= (index = mHal.indexOfKey(id)) ) return mHal.valueAt(index);
                                                return NULL;
                                            }

        typename MapT::value_type           valueAt(size_t index) const
                                            {
                                                if  ( mApp.size() > index ) return mApp.valueAt(index);
                                                index -= mApp.size();
                                                if  ( mHal.size() > index ) return mHal.valueAt(index);
                                                return NULL;
                                            }
    };

protected:  ////                            Data Members.
    Set<IMetaStreamInfo>                    mSetMeta;
    Set<IImageStreamInfo>                   mSetImage;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  IStreamInfoSetControl Interface.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////                            Operations.
    static  IStreamInfoSetControl*          create()            { return new IStreamInfoSetControl; }

    virtual Map<IMetaStreamInfo> const&     getAppMeta() const  { return mSetMeta.mApp; }
    virtual Map<IMetaStreamInfo> const&     getHalMeta() const  { return mSetMeta.mHal; }
    virtual Map<IImageStreamInfo>const&     getAppImage()const  { return mSetImage.mApp; }
    virtual Map<IImageStreamInfo>const&     getHalImage()const  { return mSetImage.mHal; }

    virtual Map<IMetaStreamInfo>&           editAppMeta()       { return mSetMeta.mApp; }
    virtual Map<IMetaStreamInfo>&           editHalMeta()       { return mSetMeta.mHal; }
    virtual Map<IImageStreamInfo>&          editAppImage()      { return mSetImage.mApp; }
    virtual Map<IImageStreamInfo>&          editHalImage()      { return mSetImage.mHal; }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  IStreamInfoSet Interface.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////                            Operations.
#define _IMPLEMENT_(_type_) \
    virtual size_t \
    get##_type_##InfoNum() const { return mSet##_type_.size(); } \
    \
    virtual android::sp<I##_type_##StreamInfo> \
    get##_type_##InfoFor(StreamId_T id) const { return mSet##_type_.valueFor(id); } \
    \
    virtual android::sp<I##_type_##StreamInfo> \
    get##_type_##InfoAt(size_t index) const   { return mSet##_type_.valueAt(index); }

        _IMPLEMENT_(Meta)   // IMetaStreamInfo, mAppMeta,  mHalMeta
        _IMPLEMENT_(Image)  //IImageStreamInfo, mAppImage, mHalImage

#undef  _IMPLEMENT_

public:     ////                            Operations.

    virtual MBOOL
    isSubsetOf(android::SortedVector<StreamId_T>const& vTest) const
    {
        if  ( vTest.size() < mSetMeta.mApp.size() + mSetMeta.mHal.size() + mSetImage.mApp.size() + mSetImage.mHal.size() )
        {
            return MFALSE;
        }
        //
        for (size_t i = 0; i < mSetMeta.mApp.size(); i++) {
            if  ( 0 > vTest.indexOf(mSetMeta.mApp.valueAt(i)->getStreamId()) ) {
                return MFALSE;
            }
        }
        for (size_t i = 0; i < mSetMeta.mHal.size(); i++) {
            if  ( 0 > vTest.indexOf(mSetMeta.mHal.valueAt(i)->getStreamId()) ) {
                return MFALSE;
            }
        }
        for (size_t i = 0; i < mSetImage.mApp.size(); i++) {
            if  ( 0 > vTest.indexOf(mSetImage.mApp.valueAt(i)->getStreamId()) ) {
                return MFALSE;
            }
        }
        for (size_t i = 0; i < mSetImage.mHal.size(); i++) {
            if  ( 0 > vTest.indexOf(mSetImage.mHal.valueAt(i)->getStreamId()) ) {
                return MFALSE;
            }
        }
        //
        return MTRUE;
    }

    virtual MBOOL
    isSupersetOf(android::SortedVector<StreamId_T>const& vTest) const
    {
        if  ( vTest.size() > mSetMeta.mApp.size() + mSetMeta.mHal.size() + mSetImage.mApp.size() + mSetImage.mHal.size() )
        {
            return MFALSE;
        }
        //
        for (size_t i = 0; i < vTest.size(); i++) {
            if  ( 0 <= mSetMeta.mApp.indexOfKey(vTest[i]) ) {
                continue;
            }
            if  ( 0 <= mSetMeta.mHal.indexOfKey(vTest[i]) ) {
                continue;
            }
            if  ( 0 <= mSetImage.mApp.indexOfKey(vTest[i]) ) {
                continue;
            }
            if  ( 0 <= mSetImage.mHal.indexOfKey(vTest[i]) ) {
                continue;
            }
            //
            return MFALSE;
        }
        //
        return MTRUE;
    }
};


/******************************************************************************
 *
 ******************************************************************************/
};  //namespace Utils
};  //namespace v3
};  //namespace NSCam
#endif  //_MTK_HARDWARE_INCLUDE_MTKCAM_V3_UTILS_STREAMINFO_ISTREAMINFOSETCONTROL_H_


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

#define LOG_TAG "MtkCam/pipeline"
//
#include "MyUtils.h"
#include "PipelineBufferSetFrameControlImp.h"
//
using namespace android;
using namespace NSCam;
using namespace NSCam::v3;
using namespace NSCam::v3::Utils;
using namespace NSCam::v3::Imp;

#define MAIN_CLASS_NAME PipelineBufferSetFrameControlImp

/******************************************************************************
 *
 ******************************************************************************/
#define MY_LOGV(fmt, arg...)        CAM_LOGV("[%s] "fmt, __FUNCTION__, ##arg)
#define MY_LOGD(fmt, arg...)        CAM_LOGD("[%s] "fmt, __FUNCTION__, ##arg)
#define MY_LOGI(fmt, arg...)        CAM_LOGI("[%s] "fmt, __FUNCTION__, ##arg)
#define MY_LOGW(fmt, arg...)        CAM_LOGW("[%s] "fmt, __FUNCTION__, ##arg)
#define MY_LOGE(fmt, arg...)        CAM_LOGE("[%s] "fmt, __FUNCTION__, ##arg)
#define MY_LOGA(fmt, arg...)        CAM_LOGA("[%s] "fmt, __FUNCTION__, ##arg)
#define MY_LOGF(fmt, arg...)        CAM_LOGF("[%s] "fmt, __FUNCTION__, ##arg)
//
#define MY_LOGV_IF(cond, ...)       do { if ( (cond) ) { MY_LOGV(__VA_ARGS__); } }while(0)
#define MY_LOGD_IF(cond, ...)       do { if ( (cond) ) { MY_LOGD(__VA_ARGS__); } }while(0)
#define MY_LOGI_IF(cond, ...)       do { if ( (cond) ) { MY_LOGI(__VA_ARGS__); } }while(0)
#define MY_LOGW_IF(cond, ...)       do { if ( (cond) ) { MY_LOGW(__VA_ARGS__); } }while(0)
#define MY_LOGE_IF(cond, ...)       do { if ( (cond) ) { MY_LOGE(__VA_ARGS__); } }while(0)
#define MY_LOGA_IF(cond, ...)       do { if ( (cond) ) { MY_LOGA(__VA_ARGS__); } }while(0)
#define MY_LOGF_IF(cond, ...)       do { if ( (cond) ) { MY_LOGF(__VA_ARGS__); } }while(0)


/******************************************************************************
 *
 ******************************************************************************/
IPipelineBufferSetFrameControl*
IPipelineBufferSetFrameControl::
create(MUINT32 frameNo, android::wp<IAppCallback>const& pAppCallback)
{
    return new MAIN_CLASS_NAME(frameNo, pAppCallback);
}


/******************************************************************************
 *
 ******************************************************************************/
MAIN_CLASS_NAME::
MAIN_CLASS_NAME(
    MUINT32 frameNo,
    android::wp<IAppCallback>const& pAppCallback
)
    : mFrameNo(frameNo)
    , mRWLock()
    , mpAppCallback(pAppCallback)
    , mListeners()
    , mTimestampFrameCreated(::elapsedRealtimeNano())
    , mTimestampFrameDone(0)
    //
    , mInfoIOMapSetMap()
    , mpStreamInfoSet(0)
    , mpPipelineNodeMap(0)
    , mpPipelineDAG(0)
    //
    , mNodeStatusMap()
    //
{
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
attachListener(
    wp<IPipelineFrameListener>const& pListener,
    MVOID* pCookie
)
{
    RWLock::AutoWLock _l(mRWLock);
    //
    mListeners.push_back(MyListener(pListener, pCookie));
    //
    return OK;
}


/******************************************************************************
 *
 ******************************************************************************/
MUINT32
MAIN_CLASS_NAME::
getFrameNo() const
{
    return mFrameNo;
}


/******************************************************************************
 *
 ******************************************************************************/
sp<IPipelineNodeMap const>
MAIN_CLASS_NAME::
getPipelineNodeMap() const
{
    RWLock::AutoRLock _l(mRWLock);
    //
    sp<IPipelineNodeMap const> p = mpPipelineNodeMap.promote();
    //
    MY_LOGE_IF(
        mpPipelineNodeMap==0 || p==0,
        "frameNo:%u Bad PipelineNodeMap: wp:%p promote:%p - "
        "TIMESTAMP(ns) created:%"PRId64" done:%"PRId64" elasped:%"PRId64,
        getFrameNo(), mpPipelineNodeMap.unsafe_get(), p.get(),
        mTimestampFrameCreated, mTimestampFrameDone,
        (mTimestampFrameDone-mTimestampFrameCreated)
    );
    //
    return p;
}


/******************************************************************************
 *
 ******************************************************************************/
IPipelineDAG const&
MAIN_CLASS_NAME::
getPipelineDAG() const
{
    RWLock::AutoRLock _l(mRWLock);
    //
    MY_LOGE_IF(
        mpPipelineDAG==0,
        "frameNo:%u NULL PipelineDAG - "
        "TIMESTAMP(ns) created:%"PRId64" done:%"PRId64" elasped:%"PRId64,
        getFrameNo(), mTimestampFrameCreated, mTimestampFrameDone,
        (mTimestampFrameDone-mTimestampFrameCreated)
    );
    return *mpPipelineDAG;
}


/******************************************************************************
 *
 ******************************************************************************/
IStreamInfoSet const&
MAIN_CLASS_NAME::
getStreamInfoSet() const
{
    RWLock::AutoRLock _l(mRWLock);
    //
    MY_LOGE_IF(
        mpStreamInfoSet==0,
        "frameNo:%u NULL StreamInfoSet - "
        "TIMESTAMP(ns) created:%"PRId64" done:%"PRId64" elasped:%"PRId64,
        getFrameNo(), mTimestampFrameCreated, mTimestampFrameDone,
        (mTimestampFrameDone-mTimestampFrameCreated)
    );
    return *mpStreamInfoSet;
}


/******************************************************************************
 *
 ******************************************************************************/
IStreamBufferSet&
MAIN_CLASS_NAME::
getStreamBufferSet() const
{
    RWLock::AutoRLock _l(mRWLock);
    return *const_cast<MAIN_CLASS_NAME*>(this);
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
setPipelineNodeMap(
    android::sp<IPipelineNodeMap const> value
)
{
    if  ( value == 0) {
        MY_LOGE("frameNo:%u - NULL value", getFrameNo());
        return BAD_VALUE;
    }
    //
    if  ( value->isEmpty() ) {
        MY_LOGE("frameNo:%u - Empty value", getFrameNo());
        return BAD_VALUE;
    }
    //
    RWLock::AutoWLock _l(mRWLock);
    mpPipelineNodeMap = value;
    return OK;
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
setPipelineDAG(android::sp<IPipelineDAG const> value)
{
    if  ( value == 0) {
        MY_LOGE("frameNo:%u - NULL value", getFrameNo());
        return BAD_VALUE;
    }
    //
    RWLock::AutoWLock _l(mRWLock);
    mpPipelineDAG = value;
    return OK;
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
setStreamInfoSet(android::sp<IStreamInfoSet const> value)
{
    if  ( value == 0) {
        MY_LOGE("frameNo:%u - NULL value", getFrameNo());
        return BAD_VALUE;
    }
    //
    RWLock::AutoWLock _l(mRWLock);
    mpStreamInfoSet = value;
    return OK;
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
queryInfoIOMapSet(
    NodeId_T const& nodeId,
    InfoIOMapSet& rIOMapSet
) const
{
    RWLock::AutoRLock _l(mRWLock);
    //
    ssize_t const index = mInfoIOMapSetMap.indexOfKey(nodeId);
    if  ( 0 > index ) {
        MY_LOGW("frameNo:%u nodeId:%#"PRIxPTR" not found", getFrameNo(), nodeId);
        return index;
    }
    //
    rIOMapSet = mInfoIOMapSetMap.valueAt(index);
    return OK;
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
addInfoIOMapSet(
    NodeId_T const& nodeId,
    InfoIOMapSet const& rIOMapSet
)
{
    RWLock::AutoWLock _l(mRWLock);
    mInfoIOMapSetMap.add(nodeId, rIOMapSet);
    return OK;
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
startConfiguration()
{
    return OK;
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
finishConfiguration()
{
    RWLock::AutoWLock _l(mRWLock);
    //
    if  ( mInfoIOMapSetMap.isEmpty() ) {
        MY_LOGE("Empty InfoIOMapSetMap");
        return NO_INIT;
    }
    //
    if  ( mpStreamInfoSet == 0 )
    {
        MY_LOGE("StreamInfoSet:%p", mpStreamInfoSet.get());
        return NO_INIT;
    }
    //
    if  ( mpPipelineDAG == 0 || mpPipelineNodeMap == 0 )
    {
        MY_LOGE("PipelineDAG:%p PipelineNodeMap:%p", mpPipelineDAG.get(), mpPipelineNodeMap.unsafe_get());
        return NO_INIT;
    }
    //
    mNodeStatusMap.setCapacity(mInfoIOMapSetMap.size());
    for (size_t i = 0; i < mInfoIOMapSetMap.size(); i++)
    {
        NodeId_T const nodeId = mInfoIOMapSetMap.keyAt(i);
        InfoIOMapSet const& rIOMapSet = mInfoIOMapSetMap.valueAt(i);
        //
        sp<NodeStatus> pNodeStatus = new NodeStatus;
        //
        //Image
        for (size_t j = 0; j < rIOMapSet.mImageInfoIOMapSet.size(); j++)
        {
            ImageInfoIOMap const& rIOMap = rIOMapSet.mImageInfoIOMapSet[j];
            //
            for (size_t k = 0; k < rIOMap.vIn.size(); k++) {
                StreamId_T const streamId = rIOMap.vIn.keyAt(k);
                sp<IImageStreamBuffer> p = getImageStreamBuffer(streamId);
                MY_LOGF_IF(p==0, "No image buffer for streamId:%#"PRIxPTR, streamId);
                pNodeStatus->mIImageSet.push_back(p);
            }
            //
            for (size_t k = 0; k < rIOMap.vOut.size(); k++) {
                StreamId_T const streamId = rIOMap.vOut.keyAt(k);
                sp<IImageStreamBuffer> p = getImageStreamBuffer(streamId);
                MY_LOGF_IF(p==0, "No image buffer for streamId:%#"PRIxPTR, streamId);
                pNodeStatus->mOImageSet.push_back(p);
            }
        }
        //
        //Meta
        for (size_t j = 0; j < rIOMapSet.mMetaInfoIOMapSet.size(); j++)
        {
            MetaInfoIOMap const& rIOMap = rIOMapSet.mMetaInfoIOMapSet[j];
            //
            for (size_t k = 0; k < rIOMap.vIn.size(); k++) {
                StreamId_T const streamId = rIOMap.vIn.keyAt(k);
                sp<IMetaStreamBuffer> p = getMetaStreamBuffer(streamId);
                MY_LOGF_IF(p==0, "No meta buffer for streamId:%#"PRIxPTR, streamId);
                pNodeStatus->mIMetaSet.push_back(p);
            }
            //
            for (size_t k = 0; k < rIOMap.vOut.size(); k++) {
                StreamId_T const streamId = rIOMap.vOut.keyAt(k);
                sp<IMetaStreamBuffer> p = getMetaStreamBuffer(streamId);
                MY_LOGF_IF(p==0, "No meta buffer for streamId:%#"PRIxPTR, streamId);
                pNodeStatus->mOMetaSet.push_back(p);
            }
        }
        //
        if  (
                ! pNodeStatus->mIMetaSet.empty()
            ||  ! pNodeStatus->mOMetaSet.empty()
            ||  ! pNodeStatus->mIImageSet.empty()
            ||  ! pNodeStatus->mOImageSet.empty()
            )
        {
            mNodeStatusMap.add(nodeId, pNodeStatus);
            mNodeStatusMap.mInFlightNodeCount++;
            //
            MY_LOGD_IF(
                1,
                "nodeId:%#"PRIxPTR" Image:I/O#=%zu/%zu Meta:I/O#=%zu/%zu (%zu %zu)",
                nodeId,
                pNodeStatus->mIImageSet.size(), pNodeStatus->mOImageSet.size(),
                pNodeStatus->mIMetaSet.size(), pNodeStatus->mOMetaSet.size(),
                rIOMapSet.mImageInfoIOMapSet.size(), rIOMapSet.mMetaInfoIOMapSet.size()
            );
        }
    }
    //
    return OK;
}


/******************************************************************************
 *
 ******************************************************************************/
sp<IUsersManager>
MAIN_CLASS_NAME::
findSubjectUsersLocked(
    StreamId_T streamId
)   const
{
#define _IMPLEMENT_(_map_) \
    { \
        ssize_t const index = _map_.indexOfKey(streamId); \
        if  ( 0 <= index ) { \
            return _map_.valueAt(index)->getUsersManager(); \
        } \
    }

    _IMPLEMENT_(mBufMap_AppImage);
    _IMPLEMENT_(mBufMap_AppMeta);
    _IMPLEMENT_(mBufMap_HalImage);
    _IMPLEMENT_(mBufMap_HalMeta);

#undef  _IMPLEMENT_

    MY_LOGW("[frameNo:%u] streamId:%#"PRIxPTR" not found", getFrameNo(), streamId);
    return NULL;
}


/******************************************************************************
 *
 ******************************************************************************/
template <class StreamBufferMapT>
sp<typename StreamBufferMapT::IStreamBufferT>
MAIN_CLASS_NAME::
getBufferLocked(
    StreamId_T streamId,
    StreamBufferMapT const& rBufMap
)   const
{
    if  ( 0 == rBufMap.mNumberOfNonNullBuffers ) {
        MY_LOGW_IF(
            0,
            "[frameNo:%u streamId:%#"PRIxPTR"] "
            "mNumberOfNonNullBuffers==0",
            getFrameNo(), streamId
        );
        return NULL;
    }
    //
    typename StreamBufferMapT::value_type pValue = rBufMap.valueFor(streamId);
    if  ( pValue == 0 ) {
        MY_LOGW_IF(
            0,
            "[frameNo:%u streamId:%#"PRIxPTR"] "
            "cannot find from map",
            getFrameNo(), streamId
        );
        return NULL;
    }
    //
    if  ( pValue->mBuffer == 0 ) {
        MY_LOGW(
            "[frameNo:%u streamId:%#"PRIxPTR"] "
            "mBitStatus(%#x) pValue->mBuffer == 0",
            getFrameNo(), streamId, pValue->mBitStatus.value
        );
        return NULL;
    }
    //
    return pValue->mBuffer;
}


/******************************************************************************
 *
 ******************************************************************************/
template <class StreamBufferMapT>
sp<typename StreamBufferMapT::IStreamBufferT>
MAIN_CLASS_NAME::
getBufferLocked(
    StreamId_T streamId,
    UserId_T userId,
    StreamBufferMapT const& rBufMap
)   const
{
    sp<typename StreamBufferMapT::IStreamBufferT>
    pBuffer = getBufferLocked(streamId, rBufMap);
    //
    if  ( pBuffer == 0 ) {
        MY_LOGW_IF(
            0,
            "[frameNo:%u streamId:%#"PRIxPTR" userId:%#"PRIxPTR"] NULL buffer",
            getFrameNo(), streamId, userId
        );
        return NULL;
    }

    /**
     * The buffer is NOT available if all users have released this buffer
     * (so as to be marked as released).
     */
    if  ( OK == pBuffer->haveAllUsersReleased() ) {
        MY_LOGW_IF(
            1,
            "[frameNo:%u streamId:%#"PRIxPTR" userId:%#"PRIxPTR"] "
            "all users released this buffer",
            getFrameNo(), streamId, userId
        );
        return NULL;
    }

    /**
     * For a specific stream buffer (associated with a stream Id), a user (with
     * a unique user Id) could successfully acquire the buffer from this buffer
     * set only if all users ahead of this user have pre-released or released
     * the buffer.
     */
    if  ( OK != pBuffer->haveAllUsersReleasedOrPreReleased(userId) ) {
        MY_LOGW_IF(
            1,
            "[frameNo:%u streamId:%#"PRIxPTR" userId:%#"PRIxPTR"] "
            "not all of prior users release or pre-release this buffer",
            getFrameNo(), streamId, userId
        );
        return NULL;
    }

    return pBuffer;
}


/******************************************************************************
 *
 ******************************************************************************/
sp<IMetaStreamBuffer>
MAIN_CLASS_NAME::
getMetaStreamBuffer(StreamId_T streamId) const
{
    sp<IMetaStreamBuffer> p;
    //
    Mutex::Autolock _lBufMapLock(mBufMapLock);
    //
    p = getBufferLocked(streamId, mBufMap_HalMeta);
    if  ( p != 0 ) {
        return p;
    }
    //
    p = getBufferLocked(streamId, mBufMap_AppMeta);
    if  ( p != 0 ) {
        return p;
    }
    //
    return NULL;
}


/******************************************************************************
 *
 ******************************************************************************/
sp<IImageStreamBuffer>
MAIN_CLASS_NAME::
getImageStreamBuffer(StreamId_T streamId) const
{
    sp<IImageStreamBuffer> p;
    //
    Mutex::Autolock _lBufMapLock(mBufMapLock);
    //
    p = getBufferLocked(streamId, mBufMap_HalImage);
    if  ( p != 0 ) {
        return p;
    }
    //
    p = getBufferLocked(streamId, mBufMap_AppImage);
    if  ( p != 0 ) {
        return p;
    }
    //
    return NULL;
}


/******************************************************************************
 *
 ******************************************************************************/
sp<IMetaStreamBuffer>
MAIN_CLASS_NAME::
getMetaBuffer(StreamId_T streamId, UserId_T userId) const
{
    sp<IMetaStreamBuffer> p;
    //
    Mutex::Autolock _lBufMapLock(mBufMapLock);
    //
    p = getBufferLocked(streamId, userId, mBufMap_HalMeta);
    if  ( p != 0 ) {
        return p;
    }
    //
    p = getBufferLocked(streamId, userId, mBufMap_AppMeta);
    if  ( p != 0 ) {
        return p;
    }
    //
    return NULL;
}


/******************************************************************************
 *
 ******************************************************************************/
sp<IImageStreamBuffer>
MAIN_CLASS_NAME::
getImageBuffer(StreamId_T streamId, UserId_T userId) const
{
    sp<IImageStreamBuffer> p;
    //
    Mutex::Autolock _lBufMapLock(mBufMapLock);
    //
    p = getBufferLocked(streamId, userId, mBufMap_HalImage);
    if  ( p != 0 ) {
        return p;
    }
    //
    p = getBufferLocked(streamId, userId, mBufMap_AppImage);
    if  ( p != 0 ) {
        return p;
    }
    //
    return NULL;
}


/******************************************************************************
 *
 ******************************************************************************/
MUINT32
MAIN_CLASS_NAME::
markUserStatus(
    StreamId_T const streamId,
    UserId_T userId,
    MUINT32 eStatus
)
{
    android::Mutex::Autolock _l(mBufMapLock);
    //
    sp<IUsersManager> pSubjectUsers = findSubjectUsersLocked(streamId);
    if  ( pSubjectUsers == 0 ) {
        return NAME_NOT_FOUND;
    }
    //
    return pSubjectUsers->markUserStatus(userId, eStatus);
}


/******************************************************************************
 *
 ******************************************************************************/
MERROR
MAIN_CLASS_NAME::
setUserReleaseFence(
    StreamId_T const streamId,
    UserId_T userId,
    MINT releaseFence
)
{
    android::Mutex::Autolock _l(mBufMapLock);
    //
    sp<IUsersManager> pSubjectUsers = findSubjectUsersLocked(streamId);
    if  ( pSubjectUsers == 0 ) {
        return NAME_NOT_FOUND;
    }
    //
    return pSubjectUsers->setUserReleaseFence(userId, releaseFence);
}


/******************************************************************************
 *
 ******************************************************************************/
MUINT
MAIN_CLASS_NAME::
queryGroupUsage(
    StreamId_T const streamId,
    UserId_T userId
)   const
{
    android::Mutex::Autolock _l(mBufMapLock);
    //
    sp<IUsersManager> pSubjectUsers = findSubjectUsersLocked(streamId);
    if  ( pSubjectUsers == 0 ) {
        return 0;
    }
    //
    return pSubjectUsers->queryGroupUsage(userId);
}


/******************************************************************************
 *
 ******************************************************************************/
MINT
MAIN_CLASS_NAME::
createAcquireFence(
    StreamId_T const streamId,
    UserId_T userId
)   const
{
    android::Mutex::Autolock _l(mBufMapLock);
    //
    sp<IUsersManager> pSubjectUsers = findSubjectUsersLocked(streamId);
    if  ( pSubjectUsers == 0 ) {
        return -1;
    }
    //
    return pSubjectUsers->createAcquireFence(userId);
}


/******************************************************************************
 *
 ******************************************************************************/
MVOID
MAIN_CLASS_NAME::
updateNodeStatusLocked(NodeId_T const nodeId, android::BitSet32& rNodeStatusUpdated)
{
    typedef NodeStatus::IO ListT;
    struct UpdateNode
    {
        static
        MBOOL
        doIt(ListT& rList, MINTPTR const nodeId)
        {
            if  ( ! rList.mNotified ) {
                ListT::iterator it = rList.begin();
                for (; it != rList.end();) {
                    MUINT32 const status = (*it)->getUserStatus(nodeId);
                    if  ( 0 != (status & IUsersManager::UserStatus::RELEASE) ) {
                        it = rList.erase(it);   //remove if released
                    }
                    else {
                        ++it;
                    }
                }
                //
                if  ( rList.empty() ) {
                    rList.mNotified = MTRUE;
                    return MTRUE;
                }
            }
            return MFALSE;
        }
    };

    MBOOL isAnyUpdate = MFALSE;
    //
    ssize_t const index = mNodeStatusMap.indexOfKey(nodeId);
    if  ( index < 0 ) {
        MY_LOGE("frameNo:%u nodeId:%#"PRIxPTR" not found", getFrameNo(), nodeId);
        return;
    }
    //
    sp<NodeStatus> pNodeStatus = mNodeStatusMap.valueAt(index);
    if  ( pNodeStatus == 0 ) {
        MY_LOGE("frameNo:%u nodeId:%#"PRIxPTR" NULL buffer", getFrameNo(), nodeId);
        return;
    }
    //
    // O Image
    if  ( UpdateNode::doIt(pNodeStatus->mOImageSet, nodeId) ) {
        isAnyUpdate = MTRUE;
        rNodeStatusUpdated.markBit(IPipelineFrameListener::eMSG_ALL_OUT_IMAGE_BUFFERS_RELEASED);
        MY_LOGD_IF(0, "frameNo:%u nodeId:%#"PRIxPTR" O Image Buffers Released", getFrameNo(), nodeId);
    }
    // I Image
    if  ( UpdateNode::doIt(pNodeStatus->mIImageSet, nodeId) ) {
        isAnyUpdate = MTRUE;
        MY_LOGD_IF(0, "frameNo:%u nodeId:%#"PRIxPTR" I Image Buffers Released", getFrameNo(), nodeId);
    }
    // O Meta
    if  ( UpdateNode::doIt(pNodeStatus->mOMetaSet, nodeId) ) {
        isAnyUpdate = MTRUE;
        rNodeStatusUpdated.markBit(IPipelineFrameListener::eMSG_ALL_OUT_META_BUFFERS_RELEASED);
        MY_LOGD_IF(0, "frameNo:%u nodeId:%#"PRIxPTR" O Meta Buffers Released", getFrameNo(), nodeId);
    }
    // I Meta
    if  ( UpdateNode::doIt(pNodeStatus->mIMetaSet, nodeId) ) {
        isAnyUpdate = MTRUE;
        MY_LOGD_IF(0, "frameNo:%u nodeId:%#"PRIxPTR" I Meta Buffers Released", getFrameNo(), nodeId);
    }
    //
    // Is it a new node with all buffers released?
    if  (
            isAnyUpdate
        &&  pNodeStatus->mOImageSet.empty()
        &&  pNodeStatus->mIImageSet.empty()
        &&  pNodeStatus->mOMetaSet.empty()
        &&  pNodeStatus->mIMetaSet.empty()
        )
    {
        mNodeStatusMap.mInFlightNodeCount--;
    }
    //
    // Is the entire frame released?
    if  ( isAnyUpdate && 0 == mNodeStatusMap.mInFlightNodeCount )
    {
        rNodeStatusUpdated.markBit(IPipelineFrameListener::eMSG_FRAME_RELEASED);
        //
        mTimestampFrameDone = ::elapsedRealtimeNano();
        //
#if 1
//        mpPipelineNodeMap = 0;
//        mpPipelineDAG = 0;
        mpStreamInfoSet = 0;
#endif
        MY_LOGD_IF(
            1,
            "Done frameNo:%u @ nodeId:%#"PRIxPTR" - timestamp: %"PRIu64"=%"PRIu64"-%"PRIu64,
            getFrameNo(), nodeId,
            (mTimestampFrameDone-mTimestampFrameCreated),
            mTimestampFrameDone, mTimestampFrameCreated
        );
    }
}


/******************************************************************************
 *
 ******************************************************************************/
struct MAIN_CLASS_NAME::TBufMapReleaser_Hal
{
public:     ////                        Data Members.
    MUINT32 const                       mFrameNo;

    android::List<android::sp<BufferMap_HalImageT::StreamBufferT> >
                                        mListToReturn_Image;
    BufferMap_HalImageT                 mrBufMap_Image;

    android::List<android::sp<BufferMap_HalMetaT::StreamBufferT> >
                                        mListToReturn_Meta;
    BufferMap_HalMetaT                  mrBufMap_Meta;

public:     ////    Operations.
    TBufMapReleaser_Hal(
        MUINT32 const frameNo,
        BufferMap_HalImageT& rBufMap_Image,
        BufferMap_HalMetaT& rBufMap_Meta
    )
        : mFrameNo(frameNo)
        //
        , mListToReturn_Image()
        , mrBufMap_Image(rBufMap_Image)
        //
        , mListToReturn_Meta()
        , mrBufMap_Meta(rBufMap_Meta)
        //
    {
    }

    MVOID
    run()
    {
        run(mFrameNo, mrBufMap_Image, mListToReturn_Image);
        run(mFrameNo, mrBufMap_Meta, mListToReturn_Meta);
    }

    template <class StreamBufferMapT, class StreamBufferListT>
    MVOID
    run(
        MUINT32 const frameNo,
        StreamBufferMapT& rBufferMap,
        StreamBufferListT& rListToReturn
    )
    {
        for (size_t i = 0; i < rBufferMap.size(); i++) {
            //
            StreamId_T const streamId = rBufferMap.keyAt(i);
            //
            typename StreamBufferMapT::value_type const&
            pValue = rBufferMap.editValueAt(i);
            if  ( pValue == 0 ) {
                MY_LOGE("[frame:%u streamId:%#"PRIxPTR"] rBufferMap.editValueAt(%zu)=NULL", frameNo, streamId, i);
                continue;
            }
            //
            //Skip if NULL buffer
            if  ( pValue->mBuffer == 0 ) {
                continue;
            }
            //
            //  [Hal Stream Buffers]
            //
            //  Condition:
            //      .This buffer is not returned before.
            //      .All users of this buffer have been released.
            //
            //  Execution:
            //      .Prepare a list of buffer to return without Release Fences.
            //
            sp<typename StreamBufferMapT::StreamBufferT>& rBuffer = pValue->mBuffer;
            BitSet32& rBitStatus = pValue->mBitStatus;
            //
            //  action if not returned && all users released
            if  (
                    ( ! rBitStatus.hasBit(eBUF_STATUS_RETURN) )
                &&  ( pValue->getUsersManager()->haveAllUsersReleased() == OK )
                )
            {
                rListToReturn.push_back(rBuffer);
                rBitStatus.markBit(eBUF_STATUS_RETURN);
                //
                rBitStatus.markBit(eBUF_STATUS_RELEASE);
                rBuffer = NULL;
                rBufferMap.mNumberOfNonNullBuffers--;
            }
        }
    }

    MVOID
    handleResult()
    {
        returnBuffers(mListToReturn_Image);
        returnBuffers(mListToReturn_Meta);
    }

    template <class T>
    MVOID
    returnBuffers(
        T& listToReturn
    )
    {
        //  Return each buffer to its pool.
        typename T::iterator it = listToReturn.begin();
        for (; it != listToReturn.end(); it++) {
            if  ( (*it) != 0 ) {
                (*it)->releaseBuffer();
            }
        }
        //
        listToReturn.clear();
    }

};


/******************************************************************************
 *
 ******************************************************************************/
MVOID
MAIN_CLASS_NAME::
applyRelease(UserId_T userId)
{
    NodeId_T const nodeId = userId;
    sp<IAppCallback> pAppCallback;
    List<MyListener> listeners;
    BitSet32 nodeStatusUpdated;
    TBufMapReleaser_Hal releaserHal(getFrameNo(), mBufMap_HalImage, mBufMap_HalMeta);
    //
    String8 const logTag = String8::format("frameNo:%u nodeId:%#"PRIxPTR, getFrameNo(), nodeId);
    MY_LOGD_IF(1, "%s +", logTag.string());
    //
    {
        RWLock::AutoWLock _lRWLock(mRWLock);
        Mutex::Autolock _lBufMapLock(mBufMapLock);
        //
        //  Update buffer maps.
        releaserHal.run();
        pAppCallback = mpAppCallback.promote();
        //
        //  Update buffer status of each node.
        updateNodeStatusLocked(nodeId, nodeStatusUpdated);
        if  ( ! nodeStatusUpdated.isEmpty() ) {
            listeners = mListeners;
        }
    }
    //
    //  Release Hal Buffers.
    releaserHal.handleResult();
    //
    //  Callback to App.
    if  ( pAppCallback == 0 ) {
        MY_LOGW("Caonnot promote AppCallback for frameNo:%u, userId:%#"PRIxPTR, getFrameNo(), userId);
    }
    else {
        pAppCallback->updateFrame(getFrameNo(), userId);
    }
    //
    //  Callback to listeners if needed.
    if  ( ! nodeStatusUpdated.isEmpty() )
    {
        NSCam::Utils::CamProfile profile(__FUNCTION__, logTag.string());
        //
        List<MyListener>::iterator it = listeners.begin();
        for (; it != listeners.end(); ++it) {
            sp<MyListener::IListener> p = it->mpListener.promote();
            if  ( p == 0 ) {
                continue;
            }
            //
            if  ( nodeStatusUpdated.hasBit(IPipelineFrameListener::eMSG_ALL_OUT_META_BUFFERS_RELEASED) ) {
                MY_LOGD_IF(0, "%s O Meta Buffers Released", logTag.string());
                p->onPipelineFrame(
                    getFrameNo(),
                    nodeId,
                    IPipelineFrameListener::eMSG_ALL_OUT_META_BUFFERS_RELEASED,
                    it->mpCookie
                );
            }
            //
            if  ( nodeStatusUpdated.hasBit(IPipelineFrameListener::eMSG_ALL_OUT_IMAGE_BUFFERS_RELEASED) ) {
                MY_LOGD_IF(0, "%s O Image Buffers Released", logTag.string());
                p->onPipelineFrame(
                    getFrameNo(),
                    nodeId,
                    IPipelineFrameListener::eMSG_ALL_OUT_IMAGE_BUFFERS_RELEASED,
                    it->mpCookie
                );
            }
            //
            if  ( nodeStatusUpdated.hasBit(IPipelineFrameListener::eMSG_FRAME_RELEASED) ) {
                MY_LOGD_IF(0, "%s Frame Done", logTag.string());
                p->onPipelineFrame(
                    getFrameNo(),
                    IPipelineFrameListener::eMSG_FRAME_RELEASED,
                    it->mpCookie
                );
            }
        }
        //
        profile.print_overtime(3, "notify listeners (nodeStatusUpdated:%#x)", nodeStatusUpdated.value);
    }
    //
    MY_LOGD_IF(1, "%s -", logTag.string());
}


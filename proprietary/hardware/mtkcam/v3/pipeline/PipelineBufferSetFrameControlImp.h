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

#ifndef _MTK_HARDWARE_MTKCAM_V3_PIPELINE_PIPELINEBUFFERSETFRAMECONTROLIMP_H_
#define _MTK_HARDWARE_MTKCAM_V3_PIPELINE_PIPELINEBUFFERSETFRAMECONTROLIMP_H_
//
#include <mtkcam/v3/pipeline/IPipelineBufferSetFrameControl.h>


/******************************************************************************
 *
 ******************************************************************************/
namespace NSCam {
namespace v3 {
namespace Imp {


/**
 * An Implementation of Pipeline Buffer Set Frame Control.
 */
class PipelineBufferSetFrameControlImp
    : public IPipelineBufferSetFrameControl
{
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  Definitions: Buffer Map Template
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////

    enum {
        eBUF_STATUS_RELEASE         = 0,
        eBUF_STATUS_RETURN,
    };

    template <class StreamBufferT>
    struct  THolder
        : public android::LightRefBase<THolder<StreamBufferT> >
    {
    public:     ////                Data Members.
        android::sp<StreamBufferT>  mBuffer;
        android::BitSet32           mBitStatus;

    public:     ////                Operations.
                                    THolder(
                                        android::sp<StreamBufferT> buffer
                                    )
                                        : mBuffer(buffer)
                                        , mBitStatus(0)
                                    {
                                    }

        IUsersManager*              getUsersManager() const
                                    {
                                        return mBuffer.get();
                                    }

    };

    template <
        class _StreamBufferT_,
        class _IStreamBufferT_ = typename _StreamBufferT_::IStreamBufferT
    >
    struct TBufferMap
        : public android::DefaultKeyedVector<
                    StreamId_T,
                    android::sp<THolder<_StreamBufferT_> >
                 >
    {
    public:     ////                Definitions.
        typedef _StreamBufferT_     StreamBufferT;
        typedef _IStreamBufferT_    IStreamBufferT;

    public:     ////                Data Members.
        ssize_t                     mNumberOfNonNullBuffers;

    public:     ////                Operations.
                                    TBufferMap()
                                        : mNumberOfNonNullBuffers(0)
                                    {}
    };

    template <
        class _StreamBufferMapT_,
        class _StreamBufferT_ = typename _StreamBufferMapT_::StreamBufferT
    >
    class MyMap : public IMap<_StreamBufferT_>
    {
    public:     ////                Definitions.
        typedef _StreamBufferMapT_  StreamBufferMapT;
        typedef _StreamBufferT_     StreamBufferT;

    protected:  ////                Data Members.
        StreamBufferMapT&           mrBufMap;

    public:     ////                Operations.
                                    MyMap(StreamBufferMapT& rBufMap)
                                        : mrBufMap(rBufMap)
                                    {}

        virtual ssize_t             add(
                                        android::sp<StreamBufferT> pBuffer
                                    )
                                    {
                                        if  ( pBuffer == 0 ) {
                                            return BAD_VALUE;
                                        }
                                        //
                                        StreamId_T const streamId = pBuffer->getStreamInfo()->getStreamId();
                                        //
                                        mrBufMap.mNumberOfNonNullBuffers++;
                                        mrBufMap.add(
                                            streamId,
                                            new THolder<StreamBufferT>(pBuffer)
                                        );
                                        //
                                        return mrBufMap.mNumberOfNonNullBuffers;
                                    }

        virtual ssize_t             setCapacity(size_t size)
                                    {
                                        return mrBufMap.setCapacity(size);
                                    }

        virtual bool                isEmpty() const
                                    {
                                        return mrBufMap.isEmpty();
                                    }

        virtual size_t              size() const
                                    {
                                        return mrBufMap.size();
                                    }

        virtual ssize_t             indexOfKey(StreamId_T const key) const
                                    {
                                        return mrBufMap.indexOfKey(key);
                                    }

        virtual StreamId_T          keyAt(size_t index) const
                                    {
                                        return mrBufMap.keyAt(index);
                                    }

        virtual android::sp<StreamBufferT>const&
                                    valueAt(size_t index) const
                                    {
                                        return mrBufMap.valueAt(index)->mBuffer;
                                    }

        virtual android::sp<StreamBufferT>const&
                                    valueFor(StreamId_T const key) const
                                    {
                                        return mrBufMap.valueFor(key)->mBuffer;
                                    }
    };

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  Definitions: Frame Listener
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////
    struct  MyListener
    {
        typedef IPipelineFrameListener
                                    IListener;
        android::wp<IListener>      mpListener;
        MVOID*                      mpCookie;
                                    //
                                    MyListener(
                                        android::wp<IListener> listener = NULL,
                                        MVOID*const cookie = NULL
                                    )
                                        : mpListener(listener)
                                        , mpCookie(cookie)
                                    {}
    };

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  Definitions: Node Buffer Status
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////
    struct  NodeStatus
        : public android::LightRefBase<NodeStatus>
    {
        struct  IO
            : public android::List<android::sp<IStreamBuffer> >
        {
            MBOOL                   mNotified;
                                    //
                                    IO()
                                        : mNotified(MFALSE)
                                    {}
        };

        IO                          mIImageSet;
        IO                          mOImageSet;
        IO                          mIMetaSet;
        IO                          mOMetaSet;
    };

    struct  NodeStatusMap
        : public android::KeyedVector<NodeId_T, android::sp<NodeStatus> >
    {
        size_t                      mInFlightNodeCount;
                                    //
                                    NodeStatusMap()
                                        : mInFlightNodeCount(0)
                                    {}
    };

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  Data Members.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
protected:  ////                    Common.
    MUINT32 const                   mFrameNo;
    mutable android::RWLock         mRWLock;
    android::wp<IAppCallback>const  mpAppCallback;
    android::List<MyListener>       mListeners;
    nsecs_t                         mTimestampFrameCreated;
    nsecs_t                         mTimestampFrameDone;

protected:  ////                    Configuration.
    android::KeyedVector<NodeId_T, InfoIOMapSet>
                                    mInfoIOMapSetMap;

    android::sp<IStreamInfoSet const>
                                    mpStreamInfoSet;

    android::wp<IPipelineNodeMap const>
                                    mpPipelineNodeMap;

    android::sp<IPipelineDAG const> mpPipelineDAG;

protected:  ////                    Data Members.
    NodeStatusMap                   mNodeStatusMap;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  Data Members: Buffer Map
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////
    typedef TBufferMap<IImageStreamBuffer, IImageStreamBuffer>  BufferMap_AppImageT;
    typedef TBufferMap< IMetaStreamBuffer,  IMetaStreamBuffer>  BufferMap_AppMetaT;
    typedef TBufferMap<HalImageStreamBuffer>                    BufferMap_HalImageT;
    typedef TBufferMap< HalMetaStreamBuffer>                    BufferMap_HalMetaT;
    struct  TBufMapReleaser_Hal;

protected:  ////
    mutable android::Mutex          mBufMapLock;
    BufferMap_AppImageT             mBufMap_AppImage;
    BufferMap_AppMetaT              mBufMap_AppMeta;
    BufferMap_HalImageT             mBufMap_HalImage;
    BufferMap_HalMetaT              mBufMap_HalMeta;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  Implementations
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////                    Operations.
                                    PipelineBufferSetFrameControlImp(
                                        MUINT32 frameNo,
                                        android::wp<IAppCallback>const& pAppCallback
                                    );

protected:  ////                    Operations (Stream Buffer Map)
    android::sp<IUsersManager>      findSubjectUsersLocked(
                                        StreamId_T streamId
                                    )   const;

    template <class StreamBufferMapT>
    android::sp<typename StreamBufferMapT::IStreamBufferT>
                                    getBufferLocked(
                                        StreamId_T streamId,
                                        StreamBufferMapT const& rBufMap
                                    )   const;

    template <class StreamBufferMapT>
    android::sp<typename StreamBufferMapT::IStreamBufferT>
                                    getBufferLocked(
                                        StreamId_T streamId,
                                        UserId_T userId,
                                        StreamBufferMapT const& rBufMap
                                    )   const;

    virtual android::sp<IMetaStreamBuffer>
                                    getMetaStreamBuffer(
                                        StreamId_T streamId
                                    )   const;

    virtual android::sp<IImageStreamBuffer>
                                    getImageStreamBuffer(
                                        StreamId_T streamId
                                    )   const;

protected:  ////                    Operations.
    MVOID                           updateNodeStatusLocked(
                                        NodeId_T const nodeId,
                                        android::BitSet32& rNodeStatusUpdated
                                    );

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  IPipelineBufferSetFrameControl Interface.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////                    Operations (Pipeline Frame)
    virtual MERROR                  startConfiguration();
    virtual MERROR                  finishConfiguration();

    virtual MERROR                  setPipelineNodeMap(
                                        android::sp<IPipelineNodeMap const> value
                                    );

    virtual MERROR                  setPipelineDAG(
                                        android::sp<IPipelineDAG const> value
                                    );

    virtual MERROR                  setStreamInfoSet(
                                        android::sp<IStreamInfoSet const> value
                                    );

    virtual MERROR                  addInfoIOMapSet(
                                        NodeId_T const& nodeId,
                                        InfoIOMapSet const& rIOMapSet
                                    );

public:     ////                    Operations (Stream Buffer Map)

    virtual android::sp<IMap<HalImageStreamBuffer> >
            editMap_HalImage()
            {
                return new MyMap<BufferMap_HalImageT>(mBufMap_HalImage);
            }

    virtual android::sp<IMap<HalMetaStreamBuffer> >
            editMap_HalMeta()
            {
                return new MyMap<BufferMap_HalMetaT>(mBufMap_HalMeta);
            }

    virtual android::sp<IMap<IImageStreamBuffer> >
            editMap_AppImage()
            {
                return new MyMap<BufferMap_AppImageT>(mBufMap_AppImage);
            }

    virtual android::sp<IMap<IMetaStreamBuffer> >
            editMap_AppMeta()
            {
                return new MyMap<BufferMap_AppMetaT>(mBufMap_AppMeta);
            }

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  IPipelineFrame Interface.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////                    Operations.
    virtual MUINT32                 getFrameNo() const;

    virtual MERROR                  attachListener(
                                        android::wp<IPipelineFrameListener>const& pListener,
                                        MVOID*const pCookie
                                    );

    virtual android::sp<IPipelineNodeMap const>
                                    getPipelineNodeMap() const;
    virtual IPipelineDAG const&     getPipelineDAG() const;
    virtual IStreamInfoSet const&   getStreamInfoSet() const;
    virtual IStreamBufferSet&       getStreamBufferSet() const;

    virtual MERROR                  queryInfoIOMapSet(
                                        NodeId_T const& nodeId,
                                        InfoIOMapSet& rIOMapSet
                                    ) const;

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  IStreamBufferSet Interface.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////                    Operations.
    virtual MVOID                   applyPreRelease(UserId_T userId)    { applyRelease(userId); }
    virtual MVOID                   applyRelease(UserId_T userId);

    virtual MUINT32                 markUserStatus(
                                        StreamId_T const streamId,
                                        UserId_T userId,
                                        MUINT32 eStatus
                                    );

    virtual MERROR                  setUserReleaseFence(
                                        StreamId_T const streamId,
                                        UserId_T userId,
                                        MINT releaseFence
                                    );

    virtual MUINT                   queryGroupUsage(
                                        StreamId_T const streamId,
                                        UserId_T userId
                                    )   const;

    virtual MINT                    createAcquireFence(
                                        StreamId_T const streamId,
                                        UserId_T userId
                                    )   const;

public:     ////                    Operations.
    virtual android::sp<IMetaStreamBuffer>
                                    getMetaBuffer(
                                        StreamId_T streamId,
                                        UserId_T userId
                                    )   const;

    virtual android::sp<IImageStreamBuffer>
                                    getImageBuffer(
                                        StreamId_T streamId,
                                        UserId_T userId
                                    )   const;

};







#if 0


class StreamBufferSetControlImp
    : public IStreamBufferSetControl
{


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//  IUsersManager Interface.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
public:     ////                            Operations.




public:     ////                            Operations.


};



#endif









/******************************************************************************
 *
 ******************************************************************************/
};  //namespace Imp
};  //namespace v3
};  //namespace NSCam
#endif  //_MTK_HARDWARE_MTKCAM_V3_PIPELINE_PIPELINEBUFFERSETFRAMECONTROLIMP_H_


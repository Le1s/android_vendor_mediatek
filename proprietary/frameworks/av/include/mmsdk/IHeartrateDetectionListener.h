/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef _MEDIATEK_MMSDK_IHEARTRATE_DETECTION_LISTENER_H_
#define _MEDIATEK_MMSDK_IHEARTRATE_DETECTION_LISTENER_H_

#include <binder/IInterface.h>
#include <stdint.h>
#include <utils/RefBase.h>
#include <ui/Rect.h>

namespace android {

class Parcel;

namespace NSMMSdk {
namespace NSHeartrate {

struct HeartrateDetectionEvent
{
    Rect                   boundBox; 
    float                  confidence; 
    int                    id; 
    int                    heartbeats;

public:
    HeartrateDetectionEvent()
    :  boundBox(Rect())
    ,  confidence(0.0)
    ,  id(0)
    ,  heartbeats(0)
    {}; 

    HeartrateDetectionEvent(
        Rect _boundBox, 
        float _confidence, 
        int   _id, 
        int   _heartbeats
    )
    : boundBox(_boundBox)
    , confidence(_confidence)
    , id(_id)
    , heartbeats(_heartbeats)
    {}; 
};



class IHeartrateDetectionListener: public IInterface
{
public:
    DECLARE_META_INTERFACE(HeartrateDetectionListener);

    virtual void onHeartrateDetected(HeartrateDetectionEvent const &event) = 0;
};

// ----------------------------------------------------------------------------

class BnHeartrateDetectionListener: public BnInterface<IHeartrateDetectionListener>
{
public:
    virtual status_t    onTransact( uint32_t code,
                                    const Parcel& data,
                                    Parcel* reply,
                                    uint32_t flags = 0);
};

}; // namespace NSHeartrate 
}; // namespace NSMMSdk
}; // namespace android

#endif

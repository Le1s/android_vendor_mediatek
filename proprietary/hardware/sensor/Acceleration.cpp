/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2012. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

#include <fcntl.h>
#include <errno.h>
#include <math.h>
#include <poll.h>
#include <unistd.h>
#include <dirent.h>
#include <sys/select.h>
#include <cutils/log.h>
#include "Acceleration.h"
#include <utils/SystemClock.h>
#include <utils/Timers.h>

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "Accel"
#endif

#define IGNORE_EVENT_TIME 350000000
#define SYSFS_PATH           "/sys/class/input"

/*****************************************************************************/
AccelerationSensor::AccelerationSensor()
    : SensorBase(NULL, "m_acc_input"),//ACC_INPUTDEV_NAME
      mEnabled(0),
      mOrientationEnabled(0),
      mInputReader(32)
{
    mPendingEvent.version = sizeof(sensors_event_t);
    mPendingEvent.sensor = ID_ACCELEROMETER;
    mPendingEvent.type = SENSOR_TYPE_ACCELEROMETER;
    mPendingEvent.acceleration.status = SENSOR_STATUS_ACCURACY_HIGH;
    memset(mPendingEvent.data, 0x00, sizeof(mPendingEvent.data));
    mEnabledTime =0;
    mDataDiv = 1;
    mPendingEvent.timestamp =0;
    input_sysfs_path_len = 0;
    input_sysfs_path[PATH_MAX];
    memset(input_sysfs_path, 0, PATH_MAX);
	m_acc_last_ts = 0;
    m_acc_delay= 0;
    char datapath[64]={"/sys/class/misc/m_acc_misc/accactive"};
    int fd = -1;
    char buf[64]={0};

    mdata_fd = FindDataFd();
    if (mdata_fd >= 0) {
        strcpy(input_sysfs_path, "/sys/class/misc/m_acc_misc/");
        input_sysfs_path_len = strlen(input_sysfs_path);
    }
    else
    {
        ALOGE("couldn't find input device ");
        return;
    }
    ALOGD("acc misc path =%s", input_sysfs_path);

    fd = open(datapath, O_RDWR);
    if (fd >= 0)
    {
    read(fd,buf,sizeof(buf));
    sscanf(buf, "%d", &mDataDiv);
    ALOGD("read div buf(%s), mdiv %d", datapath, mDataDiv);
    close(fd);
    }
    else
    {
    ALOGE("open acc misc path %s fail ", datapath);
    }
}

AccelerationSensor::~AccelerationSensor() {
    if (mdata_fd >= 0)
        close(mdata_fd);
}

int AccelerationSensor::FindDataFd() {
    int fd = -1;
    int num = -1;
    char buf[64]={0};
    const char *devnum_dir = NULL;
    char buf_s[64] = {0};


    devnum_dir = "/sys/class/misc/m_acc_misc/accdevnum";

    fd = open(devnum_dir, O_RDONLY);
    if (fd >= 0)
    {
        read(fd, buf, sizeof(buf));
        sscanf(buf, "%d\n", &num);
        close(fd);
    }else{
        return -1;
    }
    sprintf(buf_s, "/dev/input/event%d", num);
    fd = open(buf_s, O_RDONLY);
    ALOGE_IF(fd<0, "couldn't find input device");
    return fd;
}
int AccelerationSensor::enableNoHALDataAcc(int en)
{
    int fd = 0;
    char buf[2] = {0};
    ALOGD("ACC enable nodata en(%d) \r\n",en);
    strcpy(&input_sysfs_path[input_sysfs_path_len], "accenablenodata");
    ALOGD("path:%s \r\n",input_sysfs_path);
    fd = open(input_sysfs_path, O_RDWR);
    if(fd<0)
    {
          ALOGD("no ACC enable nodata control attr\r\n" );
          return -1;
    }

    buf[1] = 0;
    if(1==en)
    {
        buf[0] = '1';
    }
    if(0==en)
    {
        buf[0] = '0';
    }

    write(fd, buf, sizeof(buf));
      close(fd);

    ALOGD("ACC enable nodata done");
    return 0;
}

int AccelerationSensor::enable(int32_t handle, int en)
{
    int fd = -1;
    int flags = en ? 1 : 0;
    char buf[2] = {0};

    ALOGD("ACC enable: handle:%d, en:%d \r\n",handle,en);
    strcpy(&input_sysfs_path[input_sysfs_path_len], "accactive");
    ALOGD("path:%s \r\n",input_sysfs_path);
    fd = open(input_sysfs_path, O_RDWR);
    if(fd<0)
    {
          ALOGD("no ACC enable control attr\r\n" );
          return -1;
    }

    mEnabled = flags;
    buf[1] = 0;
    if (flags)
    {
         buf[0] = '1';
         mEnabledTime = getTimestamp() + IGNORE_EVENT_TIME;
		 m_acc_last_ts = 0;
    }
    else
     {
          buf[0] = '0';
    }
    write(fd, buf, sizeof(buf));
      close(fd);

    ALOGD("ACC enable(%d) done", mEnabled );
    return 0;
}
int AccelerationSensor::setDelay(int32_t handle, int64_t ns)
{
    int fd = -1;
    ALOGD("setDelay: (handle=%d, ns=%lld)",handle, ns);
	m_acc_delay = ns;
    strcpy(&input_sysfs_path[input_sysfs_path_len], "accdelay");
    fd = open(input_sysfs_path, O_RDWR);
    if(fd<0)
    {
           ALOGD("no ACC setDelay control attr \r\n" );
          return -1;
    }

    char buf[80] = {0};
    sprintf(buf, "%lld", ns);
    write(fd, buf, strlen(buf)+1);
    close(fd);
    return 0;
}

int AccelerationSensor::batch(int handle, int flags, int64_t samplingPeriodNs, int64_t maxBatchReportLatencyNs)
{
    int fd = -1;
    int flag = 0;
    char buf[2] = {0};

    ALOGE("ACC batch: handle:%d, en:%d,samplingPeriodNs:%lld maxBatchReportLatencyNs:%lld \r\n",handle, flags,samplingPeriodNs, maxBatchReportLatencyNs);

	//Don't change batch status if dry run.
	if (flags & SENSORS_BATCH_DRY_RUN)
		return 0;
	
    if(maxBatchReportLatencyNs == 0){
        flag = 0;
    }else{
        flag = 1;
    }

    strcpy(&input_sysfs_path[input_sysfs_path_len], "accbatch");
    ALOGD("path:%s \r\n",input_sysfs_path);
    fd = open(input_sysfs_path, O_RDWR);
    if(fd < 0)
    {
          ALOGD("no ACC batch control attr\r\n" );
          return -1;
    }

    buf[1] = 0;
    if (flag)
    {
         buf[0] = '1';
    }
    else
     {
          buf[0] = '0';
    }
    write(fd, buf, sizeof(buf));
      close(fd);

    ALOGD("ACC batch(%d) done", flag );
    return 0;

}

int AccelerationSensor::flush(int handle)
{
    ALOGD("handle=%d\n",handle);
    return -errno;
}

int AccelerationSensor::readEvents(sensors_event_t* data, int count)
{

    //ALOGE("fwq read Event 1\r\n");
    if (count < 1)
        return -EINVAL;

    ssize_t n = mInputReader.fill(mdata_fd);
    if (n < 0)
        return n;
    int numEventReceived = 0;
    input_event const* event;

    while (count && mInputReader.readEvent(&event)) {
        int type = event->type;
        //ALOGE("fwq1....\r\n");
        if (type == EV_ABS)
        {
            processEvent(event->code, event->value);
            //ALOGE("fwq2....\r\n");
        }
        else if (type == EV_SYN)
        {
            //ALOGE("fwq3....\r\n");
            int64_t time = android::elapsedRealtimeNano();//systemTime(SYSTEM_TIME_MONOTONIC);//timevalToNano(event->time);
            mPendingEvent.timestamp = time;
			//ALOGE("fwq5[%lld, %lld] ,delay=%lld\r\n",mPendingEvent.timestamp,m_acc_last_ts,m_acc_delay);
            if (mEnabled)
            {
                 //ALOGE("fwq4....\r\n");
                 if (mPendingEvent.timestamp >= mEnabledTime)
                 {
                    
                     float delta_mod= (float)(mPendingEvent.timestamp-m_acc_last_ts)/(float)(m_acc_delay);
	                  
					 //ALOGE("fwq delta_mod=%f \r\n",delta_mod);

					 int loopcout=delta_mod;
					 //ALOGE("fwq loopcout=%d \r\n",loopcout);
					 if(loopcout>=1 && loopcout<100)
					 {
					 	for(int i=0; i<loopcout; i++)
					 	{
					 		mPendingEvent.timestamp = time- (loopcout-i)*m_acc_delay;
							//ALOGE("fwq_n fack event [%lld ] \r\n",mPendingEvent.timestamp);
							*data++ = mPendingEvent;
							numEventReceived++;
							count--;
							if(0==count)
							{
								break;
							}
					 	}
					 }

					if(count != 0)
					{
                    	mPendingEvent.timestamp=time;
						*data++ = mPendingEvent;
                    	numEventReceived++;
					}
                 }
				 if(count != 0)
                     count--;

            }

			m_acc_last_ts = mPendingEvent.timestamp;
        }
        else if (type != EV_ABS)
        {
            ALOGE("AccelerationSensor: unknown event (type=%d, code=%d)",
                    type, event->code);
        }
        mInputReader.next();
    }
    //ALOGE("fwq read Event 2\r\n");
    return numEventReceived;
}

void AccelerationSensor::processEvent(int code, int value)
{
    //ALOGD("processEvent code=%d,value=%d\r\n",code, value);
    switch (code) {
    case EVENT_TYPE_ACCEL_STATUS:
            mPendingEvent.acceleration.status = value;
            break;
        case EVENT_TYPE_ACCEL_X:
            mPendingEvent.acceleration.x = (float)value / mDataDiv ;
            break;
        case EVENT_TYPE_ACCEL_Y:
            mPendingEvent.acceleration.y = (float)value / mDataDiv;
            break;
        case EVENT_TYPE_ACCEL_Z:
            mPendingEvent.acceleration.z = (float)value / mDataDiv;
            break;
    }
}

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	main_pq.cpp 

LOCAL_C_INCLUDES += \
        $(MTK_PATH_SOURCE)/hardware/dpframework/inc \
        $(TOP)/bionic \
        $(TOP)/frameworks/base/include \
        $(MTK_PATH_PLATFORM)/kernel/drivers/dispsys \
        $(MTK_PATH_PLATFORM)/hardware/pq \
        $(MTK_PATH_PLATFORM)/hardware/pq/inc \


LOCAL_SHARED_LIBRARIES := \
    libutils \
    libcutils \
    libpq_cust

LOCAL_MODULE:= pq

LOCAL_MODULE_CLASS := EXECUTABLES

include $(BUILD_EXECUTABLE)

include $(call all-makefiles-under,$(LOCAL_PATH))

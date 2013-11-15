LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := arm-cpuburn
LOCAL_SRC_FILES := arm-cpuburn.S start-cpuburn.c

include $(BUILD_SHARED_LIBRARY)

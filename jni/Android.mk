LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := cpuburn

# compile only if the CPU is supported by CPUburn assembly code
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
  LOCAL_SRC_FILES := arm-cpuburn.S cpuburn-support.c
  LOCAL_STATIC_LIBRARIES := cpufeatures
else
  # for unsupported platforms, compile a stub file that tells the Java code
  # that the platform is not supported
  LOCAL_SRC_FILES := unsupported-cpu.c
endif

include $(BUILD_SHARED_LIBRARY)

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
  $(call import-module,android/cpufeatures)
endif

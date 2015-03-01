LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := CarduinoDroid
LOCAL_SRC_FILES := CarduinoDroid.cpp

include $(BUILD_SHARED_LIBRARY)

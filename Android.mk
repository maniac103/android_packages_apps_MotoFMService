LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-subdir-java-files) \
	src/com/motorola/android/fmradio/IFMRadioServiceCallback.aidl \
	src/com/motorola/android/fmradio/IFMRadioService.aidl \

LOCAL_PACKAGE_NAME := MotoFMService

include $(BUILD_PACKAGE)

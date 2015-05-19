LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := vorbis-jni
LOCAL_CFLAGS += -I$(LOCAL_PATH)/../include -fsigned-char
ifeq ($(TARGET_ARCH),arm)
	LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp
endif

LOCAL_SHARED_LIBRARIES := libogg libvorbis

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

LOCAL_SRC_FILES := \
	com_loroclip_record_encoder_VorbisEncoder.c \
	com_loroclip_decoder_VorbisDecoder.c

include $(BUILD_SHARED_LIBRARY)

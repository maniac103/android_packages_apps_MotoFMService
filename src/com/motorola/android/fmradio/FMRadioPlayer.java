package com.motorola.android.fmradio;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

public class FMRadioPlayer {
    private static final String TAG = "FMRadioPlayer";
    private static final boolean LOGV = false;

    private EventHandler mEventHandler;
    private int mNativeContext;
    private OnCommandCompleteListener mOnCommandCompleteListener;

    public interface OnCommandCompleteListener {
        public void onCommandComplete(int arg1, int arg2, Object arg3);
    };

    private static class EventHandler extends Handler {
        private static final int FM_EVENT_CMD_DONE = 0;
        private static final int FM_EVENT_CMD_ALREADY_DONE = 1;
        private static final int FM_EVENT_CMD_NOT_SUPPORTED = 2;
        private static final int FM_EVENT_MONO_STEREO_MODE_CHANGED = 3;
        private static final int FM_EVENT_HARDWARE_ERR = 10;
        private static final int FM_EVENT_AUDIO_PATH_CHANGED = 11;

        private FMRadioPlayer mFMRadioPlayer;

        public EventHandler(FMRadioPlayer mp, Looper looper) {
            super(looper);
            mFMRadioPlayer = mp;
        }

        public void handleMessage(Message msg) {
            if (mFMRadioPlayer.mNativeContext == 0) {
                Log.w(TAG, "FMRadioCore went away with unhandled events!");
                return;
            }

            switch (msg.what) {
                case FM_EVENT_CMD_DONE:
                case FM_EVENT_CMD_ALREADY_DONE:
                    if (mFMRadioPlayer.mOnCommandCompleteListener != null) {
                        mFMRadioPlayer.mOnCommandCompleteListener.onCommandComplete(
                                msg.arg1, msg.arg2, msg.obj);
                    }
                    break;
                case FM_EVENT_HARDWARE_ERR:
                    Log.w(TAG, "Got hardware error event");
                    break;
                case FM_EVENT_AUDIO_PATH_CHANGED:
                    Log.w(TAG, "Got audio path changed event");
                    break;
                default:
                    Log.e(TAG, "Unknown message type " + msg.what);
                    break;
            }
        }
    }

    static {
        try {
            System.loadLibrary("fmradio_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Could not load native library fmradio_jni");
        }
    }

    public FMRadioPlayer() {
        Looper looper = Looper.myLooper();
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        if (looper != null) {
            mEventHandler = new EventHandler(this, looper);
            native_setup(new WeakReference(this));
        }
    }

    protected void finalize() {
        native_finalize();
    }

    public void setOnCommandCompleteListener(OnCommandCompleteListener l) {
        mOnCommandCompleteListener = l;
    }

    private static void postEventFromNative(Object ref, int what, int arg1,
            int arg2, int arg3, String strValue) {
        if (LOGV) {
            Log.v(TAG, "native event, what = " + what + " arg1 = " + arg1 +
                    " arg2 = " + arg2 + " arg3 = " + arg3 + " value = " + strValue);
        }

        WeakReference value = (WeakReference) ref;
        FMRadioPlayer mp = (FMRadioPlayer) value.get();

        if (mp == null) {
            return;
        }

        Object obj = null;
        if (strValue != null && strValue.trim().length() != 0 && !strValue.equals(FMRadioUtil.FM_NULL)) {
            obj = strValue;
        } else {
            obj = new Integer(arg3);
        }

        Message msg = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
        mp.mEventHandler.sendMessage(msg);
    }

    private final native void native_setup(Object ref);
    private final native void native_finalize();

    public native boolean currentFreq();
    public native boolean disableRDS();
    public native boolean enableRDS(int mode);
    public native boolean getAudioMode();
    public native boolean getAudioType();
    public native boolean getRSSI();
    public native boolean getVolume();
    public native boolean isMute();
    public native boolean powerOffDevice();
    public native boolean powerOnDevice(int value);
    public native boolean seek(int value);
    public native boolean setAudioMode(int value);
    public native boolean setBand(int value);
    public native boolean setMute(int value);
    public native boolean setRSSI(int value);
    public native boolean setVolume(int value);
    public native boolean stopSeek();
    public native boolean tune(int value);
}

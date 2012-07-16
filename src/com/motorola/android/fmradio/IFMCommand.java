package com.motorola.android.fmradio;

public interface IFMCommand {
    public static final int FM_CMD_NONE = -1;
    public static final int FM_CMD_TUNE_COMPLETE = 0;
    public static final int FM_CMD_SEEK_COMPLETE = 1;
    public static final int FM_CMD_SCAN_COMPLETE = 2;
    public static final int FM_CMD_ABORT_COMPLETE = 3;
    public static final int FM_CMD_RDS_PS_AVAILABLE = 4;
    public static final int FM_CMD_RDS_RT_AVAILABLE = 5;
    public static final int FM_CMD_RDS_PI_AVAILABLE = 6;
    public static final int FM_CMD_RDS_PTY_AVAILABLE = 7;
    public static final int FM_CMD_RDS_RTPLUS_AVAILABLE = 8;
    public static final int FM_CMD_ENABLE_COMPLETE = 9;
    public static final int FM_CMD_DISABLE_COMPLETE = 10;
    public static final int FM_CMD_GET_AUDIOTYPE_DONE = 11;
    public static final int FM_CMD_GET_FREQ_DONE = 12;
    public static final int FM_CMD_GET_MUTE_DONE = 13;
    public static final int FM_CMD_GET_VOLUME_DONE = 14;
    public static final int FM_CMD_GET_AUDIOMODE_DONE = 15;
    public static final int FM_CMD_GET_RSSI_DONE = 16;
    public static final int FM_CMD_SET_AUDIOMODE_DONE = 17;
    public static final int FM_CMD_SET_AUDIOMUTE_DONE = 18;
    public static final int FM_CMD_SET_BAND_DONE = 19;
    public static final int FM_CMD_ENABLE_RDS_DONE = 20;
    public static final int FM_CMD_DISABLE_RDS_DONE = 21;
    public static final int FM_CMD_SET_VOLUME_DONE = 22;
    public static final int FM_CMD_SET_RSSI_DONE = 23;
    public static final int FM_CMD_AUDIO_MODE_CHANGED = 24;
    public static final int FM_CMD_SCANNING = 25;
}

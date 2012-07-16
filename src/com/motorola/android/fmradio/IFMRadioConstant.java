package com.motorola.android.fmradio;

public interface IFMRadioConstant {
    public static final int FMRADIO_AUDIO_MODE_MONO = 0;
    public static final int FMRADIO_AUDIO_MODE_STEREO = 1;

    public static final int FMRADIO_BAND0 = 0;
    public static final int FMRADIO_BAND0_MAX_FREQ = 108000;
    public static final int FMRADIO_BAND0_MIN_FREQ = 87500;
    public static final int FMRADIO_BAND0_STEP = 200;

    public static final int FMRADIO_BAND1 = 1;
    public static final int FMRADIO_BAND1_MAX_FREQ = 108000;
    public static final int FMRADIO_BAND1_MIN_FREQ = 87500;
    public static final int FMRADIO_BAND1_STEP = 100;

    public static final int FMRADIO_BAND2 = 2;
    public static final int FMRADIO_BAND2_MAX_FREQ = 108000;
    public static final int FMRADIO_BAND2_MIN_FREQ = 87500;
    public static final int FMRADIO_BAND2_STEP = 50;

    public static final int FMRADIO_BAND3 = 3;
    public static final int FMRADIO_BAND3_MAX_FREQ = 90000;
    public static final int FMRADIO_BAND3_MIN_FREQ = 76000;
    public static final int FMRADIO_BAND3_STEP = 100;

    public static final int FMRADIO_BAND_JAPAN = 1;
    public static final int FMRADIO_BAND_US_EUROPE = 0;

    public static final int FMRADIO_MUTE_AUDIO = 1;
    public static final int FMRADIO_MUTE_NOT = 0;

    public static final int FMRADIO_RDS_MODE_RBDS = 1;
    public static final int FMRADIO_RDS_MODE_RDS = 0;

    public static final int FMRADIO_SEEK_DIRECTION_DOWN = 1;
    public static final int FMRADIO_SEEK_DIRECTION_NONE = -1;
    public static final int FMRADIO_SEEK_DIRECTION_UP = 0;

    public static final int FMRADIO_STATUS_FAIL = 0;
    public static final int FMRADIO_STATUS_OK = 1;

    public static final int FMRADIO_TYPE_ANALOG = 0;
    public static final int FMRADIO_TYPE_DIGITAL = 1;

    public static final int FMRADIO_VOLUME_MAX = 15;
    public static final int FMRADIO_VOLUME_MIN = 0;
}

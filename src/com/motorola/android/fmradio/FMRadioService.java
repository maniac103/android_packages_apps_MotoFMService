package com.motorola.android.fmradio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemService;
import android.util.Log;

import com.motorola.android.fmradio.FMRadioPlayer.OnCommandCompleteListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FMRadioService extends Service implements IFMCommand {
    private static final String TAG = "FMRadioService";
    private static final boolean LOGV = true;

    /* service name of fmradioserver */
    private static final String SERVICE_FMRADIO = "fmradio";

    private boolean mBeginInvokeCB = false;
    private List<CBEntity> mCBList = new ArrayList<CBEntity>();
    final RemoteCallbackList<IFMRadioServiceCallback> mCallbacks =
            new RemoteCallbackList<IFMRadioServiceCallback>();
    private boolean mIgnoreCallBack = false;

    private int mBand = IFMRadioConstant.FMRADIO_BAND0;
    private int mCurrentFreq;
    private int mEdgeSeekCount = 0;
    private List<Integer> mFMCmdList = new ArrayList<Integer>();
    private FMRadioPlayer mFMRadioJNI;
    private boolean mScanning = false;
    private boolean mStopScan = true;
    private boolean mIsEnable = false;
    private boolean mRdsEnabled = false;
    private boolean mScanBegin = false;
    private boolean mSeekWrap = false;
    private boolean mPowerOffComplete = false;
    private int mSeeking = IFMRadioConstant.FMRADIO_SEEK_DIRECTION_NONE;
    private int mRdsMode = -1;
    private int mRdsPI;
    private String mRdsPS;
    private int mRdsPTY;
    private String mRdsRT;
    private String mRdsRTPlus;

    private static class CBEntity {
        int cmd;
        int status;
        Object value;

        CBEntity(int cmd, int status, Object value) {
            this.cmd = cmd;
            this.status = status;
            this.value = value;
        }
    };

    private final OnCommandCompleteListener mCommandComplListener = new OnCommandCompleteListener() {
        private boolean enableCompleted(List<CBEntity> list) {
            boolean rst = false;
            int enableCount = 0;
            int tuneCount = 0;
            int setBandCount = 0;
            int setRSSICount = 0;

            if (list == null || list.isEmpty()) {
                return false;
            }

            for (int i = 0; i < list.size(); i++) {
                int cmd = list.get(i).cmd;
                if (cmd == IFMCommand.FM_CMD_ENABLE_COMPLETE) {
                    enableCount++;
                } else if (cmd == IFMCommand.FM_CMD_TUNE_COMPLETE) {
                    tuneCount++;
                } else if (cmd == IFMCommand.FM_CMD_SET_BAND_DONE) {
                    setBandCount++;
                } else if (cmd == IFMCommand.FM_CMD_SET_RSSI_DONE) {
                    setRSSICount++;
                }
            }

            if (enableCount >= 1 && tuneCount >= 2 && setBandCount >= 1 && setRSSICount >= 1) {
                return true;
            }

            return false;
        }

        public void onCommandComplete(int cmd, int status, Object value) {
            if (LOGV) Log.v(TAG, "onCommandComplete, cmd = " + cmd + " status = " + status + " value = " + value);

            FMRadioUtil.checkCmdInList(cmd, mFMCmdList);

            switch (cmd) {
                case IFMCommand.FM_CMD_TUNE_COMPLETE:
                    if (FMRadioUtil.checkStatusAndInt(status, value)) {
                        mCurrentFreq = (Integer) value;
                        resetRdsData();
                        if (mIsEnable) {
                            mIsEnable = false;
                            FMRadioUtil.sleep(FMRadioUtil.FM_CMD_INTERVAL);
                            int bandForHAL = FMRadioUtil.getBandForStack(mBand);
                            FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SET_BAND_DONE, mFMCmdList);
                            FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                            if (!mFMRadioJNI.setBand(bandForHAL)) {
                                FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SET_BAND_DONE, mFMCmdList);
                                FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                            }
                        } else if (mScanning && mScanBegin) {
                            mIgnoreCallBack = true;
                            mScanBegin = false;
                            cmd = IFMCommand.FM_CMD_NONE;
                            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                                FMRadioUtil.sleep(FMRadioUtil.FM_CMD_INTERVAL);
                                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                                if (!mFMRadioJNI.seek(IFMRadioConstant.FMRADIO_SEEK_DIRECTION_UP)) {
                                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                                }
                            }
                        } else if (mSeeking != IFMRadioConstant.FMRADIO_SEEK_DIRECTION_NONE) {
                            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                                mIgnoreCallBack = true;
                                mSeekWrap = true;
                                FMRadioUtil.sleep(FMRadioUtil.FM_CMD_INTERVAL);
                                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                                if (!mFMRadioJNI.seek(mSeeking)) {
                                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                                }
                                mSeeking = IFMRadioConstant.FMRADIO_SEEK_DIRECTION_NONE;
                            }
                        }
                    }
                    break;
                case IFMCommand.FM_CMD_SEEK_COMPLETE:
                    resetRdsData();
                    if (!mScanning && FMRadioUtil.checkInt(value)) {
                        mCurrentFreq = (Integer) value;
                        if (status == IFMRadioConstant.FMRADIO_STATUS_FAIL) {
                            if (mSeekWrap) {
                                mSeekWrap = false;
                                mIgnoreCallBack = false;
                            } else {
                                mIgnoreCallBack = true;
                            }
                            if (mCurrentFreq == getMaxFreq() && FMRadioUtil.checkCmdListComplete(mFMCmdList) && mEdgeSeekCount == 0) {
                                mEdgeSeekCount++;
                                mSeeking = IFMRadioConstant.FMRADIO_SEEK_DIRECTION_UP;
                                FMRadioUtil.sleep(FMRadioUtil.FM_CMD_INTERVAL);
                                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                                if (!mFMRadioJNI.tune(getMinFreq())) {
                                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                                }
                            } else if (mCurrentFreq == getMaxFreq() && FMRadioUtil.checkCmdListComplete(mFMCmdList) && mEdgeSeekCount == 0) {
                                mEdgeSeekCount++;
                                mSeeking = IFMRadioConstant.FMRADIO_SEEK_DIRECTION_DOWN;
                                FMRadioUtil.sleep(FMRadioUtil.FM_CMD_INTERVAL);
                                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                                if (!mFMRadioJNI.tune(getMaxFreq())) {
                                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                                }
                            } else {
                                mEdgeSeekCount = 0;
                                mSeeking = IFMRadioConstant.FMRADIO_SEEK_DIRECTION_NONE;
                            }
                        }
                    }

                    if (mScanning) {
                        if (FMRadioUtil.checkStatusAndInt(status, value)) {
                            int soughtFreq = (Integer) value;

                            if (mCurrentFreq < soughtFreq) {
                                mCurrentFreq = soughtFreq;
                                cmd = IFMCommand.FM_CMD_SCANNING;
                                if (mStopScan) {
                                    mScanning = false;
                                    cmd = IFMCommand.FM_CMD_ABORT_COMPLETE;
                                    status = IFMRadioConstant.FMRADIO_STATUS_OK;
                                } else if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                                    FMRadioUtil.sleep(FMRadioUtil.FM_SCAN_INTERVAL);
                                    FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                                    if (!mFMRadioJNI.seek(IFMRadioConstant.FMRADIO_SEEK_DIRECTION_UP)) {
                                        FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                                    }
                                }
                            } else {
                                cmd = IFMCommand.FM_CMD_SCAN_COMPLETE;
                                status = IFMRadioConstant.FMRADIO_STATUS_OK;
                                mScanning = false;
                                value = Integer.valueOf(-1);
                            }
                        } else /* checkstatus */ {
                            cmd = IFMCommand.FM_CMD_SCAN_COMPLETE;
                            status = IFMRadioConstant.FMRADIO_STATUS_OK;
                            mScanning = false;
                            value = Integer.valueOf(-1);
                        }
                    }
                    break;
                case IFMCommand.FM_CMD_ABORT_COMPLETE:
                case IFMCommand.FM_CMD_GET_FREQ_DONE:
                    if (FMRadioUtil.checkStatusAndInt(status, value)) {
                        mCurrentFreq = (Integer) value;
                    }
                    break;
                case IFMCommand.FM_CMD_RDS_PS_AVAILABLE:
                    if (FMRadioUtil.checkStatusAndStr(status, value)) {
                        mRdsPS = (String) value;
                    }
                    break;
                case IFMCommand.FM_CMD_RDS_RT_AVAILABLE:
                    if (FMRadioUtil.checkStatusAndStr(status, value)) {
                        mRdsRT = (String) value;
                    }
                    break;
                case IFMCommand.FM_CMD_RDS_PI_AVAILABLE:
                    if (FMRadioUtil.checkStatusAndInt(status, value)) {
                        mRdsPI = (Integer) value;
                    }
                    break;
                case IFMCommand.FM_CMD_RDS_PTY_AVAILABLE:
                    if (FMRadioUtil.checkStatusAndInt(status, value)) {
                        mRdsPTY = (Integer) value;
                    }
                    break;
                case IFMCommand.FM_CMD_RDS_RTPLUS_AVAILABLE:
                    if (FMRadioUtil.checkStatusAndStr(status, value)) {
                        mRdsRTPlus = (String) value;
                    }
                    break;
                case IFMCommand.FM_CMD_ENABLE_COMPLETE:
                    mIsEnable = true;
                    break;
                case IFMCommand.FM_CMD_DISABLE_COMPLETE:
                    mPowerOffComplete = true;
                    break;
                case IFMCommand.FM_CMD_ENABLE_RDS_DONE:
                    if (FMRadioUtil.checkStatus(status)) {
                        mRdsEnabled = true;
                    }
                    break;
                case IFMCommand.FM_CMD_DISABLE_RDS_DONE:
                    if (FMRadioUtil.checkStatus(status)) {
                        mRdsEnabled = false;
                    }
                    break;
            }

            if (mBeginInvokeCB) {
                if (!mIgnoreCallBack) {
                    invokeCallback(cmd, status, value);
                } else {
                    mIgnoreCallBack = false;
                }
            } else {
                if (cmd == IFMCommand.FM_CMD_TUNE_COMPLETE || cmd == IFMCommand.FM_CMD_ENABLE_COMPLETE
                        || cmd == IFMCommand.FM_CMD_SET_BAND_DONE || cmd == IFMCommand.FM_CMD_SET_RSSI_DONE) {
                    CBEntity cb = new CBEntity(cmd, status, value);
                    mCBList.add(cb);
                }

                if (enableCompleted(mCBList)) {
                    mBeginInvokeCB = true;

                    CBEntity cbEnable = getCBForEnable(mCBList);
                    if (cbEnable != null) {
                        invokeCallback(cbEnable.cmd, cbEnable.status, cbEnable.value);
                    }

                    CBEntity cbTune = getCBForTune(mCBList);
                    if (cbTune != null) {
                        invokeCallback(cbTune.cmd, cbTune.status, cbTune.value);
                    }

                    mCBList.clear();
                }
            }
        }
    };

    private final IFMRadioService.Stub mBinder = new IFMRadioService.Stub() {
        @Override
        public boolean getAudioMode() throws RemoteException {
            boolean result = false;
            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_GET_AUDIOMODE_DONE, mFMCmdList);
                result = mFMRadioJNI.getAudioMode();
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_GET_AUDIOMODE_DONE, mFMCmdList);
                }
            }
            return result;
        }

        @Override
        public boolean getAudioType() throws RemoteException {
            boolean result = false;
            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_GET_AUDIOTYPE_DONE, mFMCmdList);
                result = mFMRadioJNI.getAudioType();
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_GET_AUDIOTYPE_DONE, mFMCmdList);
                }
            }
            return result;
        }

        @Override
        public int getBand() throws RemoteException {
            return mBand;
        }

        @Override
        public boolean getCurrentFreq() throws RemoteException {
            boolean result = false;
            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_GET_FREQ_DONE, mFMCmdList);
                result = mFMRadioJNI.currentFreq();
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_GET_FREQ_DONE, mFMCmdList);
                }
            }
            return result;
        }

        @Override
        public int getMaxFrequence() throws RemoteException {
            return getMaxFreq();
        }

        @Override
        public int getMinFrequence() throws RemoteException {
            return getMinFreq();
        }

        @Override
        public String getRDSStationName() {
            if (mRdsMode == IFMRadioConstant.FMRADIO_RDS_MODE_RDS && !mRdsPS.equals("")) {
                return mRdsPS;
            } else if (mRdsMode == IFMRadioConstant.FMRADIO_RDS_MODE_RBDS && mRdsPI != -1) {
                char[] piArray = FMRadioUtil.decodePI(mRdsPI);
                if (piArray != null) {
                    return new String(piArray);
                }
            }

            return "";
        }

        @Override
        public boolean getRSSI() throws RemoteException {
            boolean result = false;
            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_GET_RSSI_DONE, mFMCmdList);
                result = mFMRadioJNI.getRSSI();
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_GET_RSSI_DONE, mFMCmdList);
                }
            }
            return result;
        }

        @Override
        public int getRdsPI() {
            return mRdsPI;
        }

        @Override
        public String getRdsPS() throws RemoteException {
            return mRdsPS;
        }

        @Override
        public int getRdsPTY() throws RemoteException {
            return mRdsPTY;
        }

        @Override
        public String getRdsRT() throws RemoteException {
            return mRdsRT;
        }

        @Override
        public String getRdsRTPLUS() throws RemoteException {
            return mRdsRTPlus;
        }

        @Override
        public int getStepUnit() throws RemoteException {
            return getUnit();
        }

        @Override
        public boolean getVolume() throws RemoteException {
            boolean result = false;
            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_GET_VOLUME_DONE, mFMCmdList);
                result = mFMRadioJNI.getVolume();
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_GET_VOLUME_DONE, mFMCmdList);
                }
            }
            return result;
        }

        @Override
        public boolean isMute() throws RemoteException {
            boolean result = false;
            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_GET_MUTE_DONE, mFMCmdList);
                result = mFMRadioJNI.isMute();
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_GET_MUTE_DONE, mFMCmdList);
                }
            }
            return result;
        }

        @Override
        public boolean isRdsEnable() throws RemoteException {
            return mRdsEnabled;
        }

        @Override
        public void registerCallback(IFMRadioServiceCallback cb) throws RemoteException {
            if (cb != null) {
                mCallbacks.register(cb);
            }
        }

        @Override
        public void unregisterCallback(IFMRadioServiceCallback cb) throws RemoteException {
            if (cb != null) {
                mCallbacks.unregister(cb);
            }
        }

        @Override
        public boolean scan() throws RemoteException {
            boolean result = false;

            mScanning = true;
            mScanBegin = true;
            mStopScan = false;

            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                result = mFMRadioJNI.tune(getMinFreq());
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                }
            }

            return result;
        }

        @Override
        public boolean seek(int direction) throws RemoteException {
            boolean result = false;

            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                result = mFMRadioJNI.seek(direction);
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                }
            }

            return result;
        }

        @Override
        public boolean setAudioMode(int mode) throws RemoteException {
            boolean result = false;

            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SET_AUDIOMODE_DONE, mFMCmdList);
                result = mFMRadioJNI.setAudioMode(mode);
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SET_AUDIOMODE_DONE, mFMCmdList);
                }
            }

            return result;
        }

        @Override
        public boolean setBand(int band) throws RemoteException {
            boolean result = false;
            int bandForHAL = FMRadioUtil.getBandForStack(band);

            mBand = band;
            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SET_BAND_DONE, mFMCmdList);
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                result = mFMRadioJNI.setBand(bandForHAL);
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SET_BAND_DONE, mFMCmdList);
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                }
            }

            return result;
        }

        @Override
        public boolean setMute(int mode) throws RemoteException {
            boolean result = false;

            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SET_AUDIOMUTE_DONE, mFMCmdList);
                result = mFMRadioJNI.setMute(mode);
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SET_AUDIOMUTE_DONE, mFMCmdList);
                }
            }

            return result;
        }

        @Override
        public boolean setRSSI(int rssi) throws RemoteException {
            boolean result = false;

            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_SET_RSSI_DONE, mFMCmdList);
                result = mFMRadioJNI.setRSSI(rssi);
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SET_RSSI_DONE, mFMCmdList);
                }
            }

            return result;
        }

        @Override
        public boolean setRdsEnable(boolean flag, int mode) throws RemoteException {
            boolean result = false;

            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                if (flag) {
                    FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_ENABLE_RDS_DONE, mFMCmdList);
                    result = mFMRadioJNI.enableRDS(mode);
                    if (!result) {
                        FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_ENABLE_RDS_DONE, mFMCmdList);
                    } else {
                        mRdsMode = mode;
                    }
                } else {
                    FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_DISABLE_RDS_DONE, mFMCmdList);
                    result = mFMRadioJNI.disableRDS();
                    if (!result) {
                        FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_DISABLE_RDS_DONE, mFMCmdList);
                    } else {
                        mRdsMode = -1;
                    }
                }
            }
            return result;
        }

        @Override
        public boolean setVolume(int volume) throws RemoteException {
            return mFMRadioJNI.setVolume(volume);
        }

        @Override
        public boolean stopScan() throws RemoteException {
            mStopScan = true;
            return true;
        }

        @Override
        public boolean stopSeek() throws RemoteException {
            if (mFMRadioJNI.stopSeek()) {
                FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_SEEK_COMPLETE, mFMCmdList);
                return true;
            }

            return false;
        }

        @Override
        public boolean tune(int freq) throws RemoteException {
            boolean result = false;

            if (FMRadioUtil.checkCmdListComplete(mFMCmdList)) {
                FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                result = mFMRadioJNI.tune(freq);
                if (!result) {
                    FMRadioUtil.removeCmdFromList(IFMCommand.FM_CMD_TUNE_COMPLETE, mFMCmdList);
                }
            }

            return result;
        }
    };

    public FMRadioService() {
        super();

        resetRdsData();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (LOGV) Log.v(TAG, "onBind()");
        SystemService.start(SERVICE_FMRADIO);

        mFMRadioJNI = new FMRadioPlayer();
        mFMRadioJNI.setOnCommandCompleteListener(mCommandComplListener);

        mFMCmdList.clear();
        mCBList.clear();
        mBeginInvokeCB = false;
        mIgnoreCallBack = false;
        mSeekWrap = false;

        mFMRadioJNI.powerOnDevice(0);

        mBand = FMRadioUtil.getBandByLocale(getResources().getConfiguration().locale);

        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LOGV) Log.v(TAG, "onCreate()");
        mBeginInvokeCB = false;
    }

    @Override
    public void onRebind(Intent intent) {
        if (LOGV) Log.v(TAG, "onRebind()");

        SystemService.start(SERVICE_FMRADIO);
        mFMRadioJNI = new FMRadioPlayer();
        mFMRadioJNI.setOnCommandCompleteListener(mCommandComplListener);

        mFMCmdList.clear();
        mCBList.clear();
        mBeginInvokeCB = false;
        mIgnoreCallBack = false;
        mSeekWrap = false;

        mFMRadioJNI.powerOnDevice(0);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);

        if (LOGV) Log.v(TAG, "onUnbind()");
        mFMCmdList.clear();
        FMRadioUtil.addCmdToList(IFMCommand.FM_CMD_DISABLE_COMPLETE, mFMCmdList);

        mFMRadioJNI.powerOffDevice();
        FMRadioUtil.sleep(FMRadioUtil.FM_POWEROFF_TIME);
        mFMCmdList.clear();
        mCallbacks.kill();
        mFMRadioJNI.setOnCommandCompleteListener(null);
        SystemService.stop(SERVICE_FMRADIO);

        return true;
    }

    private int getMinFreq() {
        switch (mBand) {
            case IFMRadioConstant.FMRADIO_BAND0:
                return IFMRadioConstant.FMRADIO_BAND0_MIN_FREQ;
            case IFMRadioConstant.FMRADIO_BAND1:
                return IFMRadioConstant.FMRADIO_BAND1_MIN_FREQ;
            case IFMRadioConstant.FMRADIO_BAND2:
                return IFMRadioConstant.FMRADIO_BAND2_MIN_FREQ;
            case IFMRadioConstant.FMRADIO_BAND3:
                return IFMRadioConstant.FMRADIO_BAND3_MIN_FREQ;
        }
        return IFMRadioConstant.FMRADIO_BAND0_MIN_FREQ;
    }

    private int getMaxFreq() {
        switch (mBand) {
            case IFMRadioConstant.FMRADIO_BAND0:
                return IFMRadioConstant.FMRADIO_BAND0_MAX_FREQ;
            case IFMRadioConstant.FMRADIO_BAND1:
                return IFMRadioConstant.FMRADIO_BAND1_MAX_FREQ;
            case IFMRadioConstant.FMRADIO_BAND2:
                return IFMRadioConstant.FMRADIO_BAND2_MAX_FREQ;
            case IFMRadioConstant.FMRADIO_BAND3:
                return IFMRadioConstant.FMRADIO_BAND3_MAX_FREQ;
        }
        return IFMRadioConstant.FMRADIO_BAND0_MAX_FREQ;
    }

    private int getUnit() {
        switch (mBand) {
            case IFMRadioConstant.FMRADIO_BAND0:
                return IFMRadioConstant.FMRADIO_BAND0_STEP;
            case IFMRadioConstant.FMRADIO_BAND1:
                return IFMRadioConstant.FMRADIO_BAND1_STEP;
            case IFMRadioConstant.FMRADIO_BAND2:
                return IFMRadioConstant.FMRADIO_BAND2_STEP;
            case IFMRadioConstant.FMRADIO_BAND3:
                return IFMRadioConstant.FMRADIO_BAND3_STEP;
        }
        return IFMRadioConstant.FMRADIO_BAND0_STEP;
    }

    private void invokeCallback(int cmd, int status, Object value) {
        if (mCallbacks == null) {
            return;
        }

        int N = mCallbacks.beginBroadcast();
        if (LOGV) {
            Log.v(TAG, "N = " + N);
        }

        for (int i = 0; i < N; i++) {
            if (value != null) {
                try {
                    IFMRadioServiceCallback callback = mCallbacks.getBroadcastItem(i);
                    callback.onCommandComplete(cmd, status, value.toString());
                } catch (RemoteException e) {
                    Log.d(TAG, "Could not invoke service callback", e);
                }
            }
        }

        mCallbacks.finishBroadcast();
    }

    private void resetRdsData() {
        mRdsPI = -1;
        mRdsPS = "";
        mRdsPTY = -1;
        mRdsRT = "";
        mRdsRTPlus = "";
    }

    protected CBEntity getCBForEnable(List<CBEntity> cbList) {
        CBEntity cb = null;

        if (cbList == null) {
            return null;
        }

        for (CBEntity item : cbList) {
            if (item.cmd == IFMCommand.FM_CMD_ENABLE_COMPLETE) {
                cb = item;
                break;
            }
        }

        return cb;
    }

    protected CBEntity getCBForTune(List<CBEntity> cbList) {
        CBEntity cb = null;

        if (cbList == null || cbList.isEmpty()) {
            return null;
        }

        for (int i = cbList.size() - 1; i >= 0; i--) {
            CBEntity item = cbList.get(i);
            if (item.cmd == IFMCommand.FM_CMD_TUNE_COMPLETE) {
                cb = item;
                break;
            }
        }

        return cb;
    }
}

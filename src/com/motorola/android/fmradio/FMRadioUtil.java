package com.motorola.android.fmradio;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class FMRadioUtil implements IFMRadioConstant {
    private static final String TAG = "FMRadioUtil";

    public static final int FMRADIO_DEFAULT_INT = -1;
    public static final String FMRADIO_DEFAULT_STR = "";

    public static final int FM_CMD_INTERVAL = 200;
    public static final String FM_NULL = "com.motorola.android.fmradio.null";
    public static final int FM_POWEROFF_TIME = 1000;
    public static final int FM_SCAN_INTERVAL = 2000;

    private static final String GERMANY = "DEU";
    private static final String GREECE = "GRC";
    private static final String INDONESIA = "IDN";
    private static final String ITALY = "ITA";
    private static final String JAPAN = "JPN";
    private static final String KOREA = "KOR";
    private static final String[] LATIN = new String[33];
    private static final String SWITZERLAND = "CHE";
    private static final String THAILAND = "THA";
    private static final String USA = "USA";

    private static final HashMap<Integer, String> threeLetters = new HashMap<Integer, String>();

    static {
        LATIN[0] = "MEX";
        LATIN[1] = "GTM";
        LATIN[2] = "HND";
        LATIN[3] = "SLV";
        LATIN[4] = "NIC";
        LATIN[5] = "CRI";
        LATIN[6] = "PAN";
        LATIN[7] = "CUB";
        LATIN[8] = "HTI";
        LATIN[9] = "DOM";
        LATIN[10] = "JAM";
        LATIN[11] = "TTO";
        LATIN[12] = "BRB";
        LATIN[13] = "GRD";
        LATIN[14] = "DMA";
        LATIN[15] = "LCA";
        LATIN[16] = "VCT";
        LATIN[17] = "BHS";
        LATIN[18] = "GUY";
        LATIN[19] = "SUR";
        LATIN[20] = "VEN";
        LATIN[21] = "COL";
        LATIN[22] = "BRA";
        LATIN[23] = "ECU";
        LATIN[24] = "PER";
        LATIN[25] = "KNA";
        LATIN[26] = "BOL";
        LATIN[27] = "CHL";
        LATIN[28] = "ARG";
        LATIN[29] = "PRY";
        LATIN[30] = "URY";
        LATIN[31] = "BLZ";
        LATIN[32] = "ATG";

        threeLetters.put(0x9950, "KEX");
        threeLetters.put(0x9951, "KFH");
        threeLetters.put(0x9952, "KFI");
        threeLetters.put(0x9953, "KGA");
        threeLetters.put(0x9954, "KGO");
        threeLetters.put(0x9955, "KGU");
        threeLetters.put(0x9956, "KGW");
        threeLetters.put(0x9957, "KGY");
        threeLetters.put(0x9958, "KID");
        threeLetters.put(0x9959, "KIT");
        threeLetters.put(0x995a, "KJR");
        threeLetters.put(0x995b, "KLO");
        threeLetters.put(0x995c, "KLZ");
        threeLetters.put(0x995d, "KMA");
        threeLetters.put(0x995e, "KMJ");
        threeLetters.put(0x995f, "KNX");
        threeLetters.put(0x9960, "KOA");
        threeLetters.put(0x9964, "KQV");
        threeLetters.put(0x9965, "KSL");
        threeLetters.put(0x9966, "KUJ");
        threeLetters.put(0x9967, "KVI");
        threeLetters.put(0x9968, "KWG");
        threeLetters.put(0x996b, "KYW");
        threeLetters.put(0x996d, "WBZ");
        threeLetters.put(0x996e, "WDZ");
        threeLetters.put(0x996f, "WEW");
        threeLetters.put(0x9971, "WGL");
        threeLetters.put(0x9972, "WGN");
        threeLetters.put(0x9973, "WGR");
        threeLetters.put(0x9975, "WHA");
        threeLetters.put(0x9976, "WHB");
        threeLetters.put(0x9977, "WHK");
        threeLetters.put(0x9978, "WHO");
        threeLetters.put(0x997a, "WIP");
        threeLetters.put(0x997b, "WJR");
        threeLetters.put(0x997c, "WKY");
        threeLetters.put(0x997d, "WLS");
        threeLetters.put(0x997e, "WLW");
        threeLetters.put(0x9981, "WOC");
        threeLetters.put(0x9983, "WOL");
        threeLetters.put(0x9984, "WOR");
        threeLetters.put(0x9988, "WWJ");
        threeLetters.put(0x9989, "WWL");
        threeLetters.put(0x9990, "KDB");
        threeLetters.put(0x9991, "KGB");
        threeLetters.put(0x9992, "KOY");
        threeLetters.put(0x9993, "KPQ");
        threeLetters.put(0x9994, "KSD");
        threeLetters.put(0x9995, "KUT");
        threeLetters.put(0x9996, "KXL");
        threeLetters.put(0x9997, "KXO");
        threeLetters.put(0x9999, "WBT");
        threeLetters.put(0x999a, "WGH");
        threeLetters.put(0x999b, "WGY");
        threeLetters.put(0x999c, "WHP");
        threeLetters.put(0x999d, "WIL");
        threeLetters.put(0x999e, "WMC");
        threeLetters.put(0x999f, "WMT");
        threeLetters.put(0x99a0, "WOI");
        threeLetters.put(0x99a1, "WOW");
        threeLetters.put(0x99a2, "WRR");
        threeLetters.put(0x99a3, "WSB");
        threeLetters.put(0x99a4, "WSM");
        threeLetters.put(0x99a5, "KBW");
        threeLetters.put(0x99a6, "KCY");
        threeLetters.put(0x99a7, "KDF");
        threeLetters.put(0x99aa, "KHQ");
        threeLetters.put(0x99ab, "KOB");
        threeLetters.put(0x99b3, "WIS");
        threeLetters.put(0x99b4, "WJW");
        threeLetters.put(0x99b5, "WJZ");
        threeLetters.put(0x99b9, "WRC");
    };

    public static void addCmdToList(int cmd, List<Integer> cmdList) {
        if (cmdList != null) {
            cmdList.add(Integer.valueOf(cmd));
        }
    }

    public static void removeCmdFromList(int cmd, List<Integer> cmdList) {
        if (cmdList == null) {
            return;
        }
        for (int i = 0; i < cmdList.size(); i++) {
            if (cmd == cmdList.get(i)) {
                cmdList.remove(i);
                break;
            }
        }
    }

    public static void checkCmdInList(int cmd, List<Integer> cmdList) {
        if (cmdList == null) {
            return;
        }

        Iterator<Integer> iter = cmdList.iterator();
        while (iter.hasNext()) {
            Integer item = iter.next();
            if (item == cmd) {
                cmdList.remove(item);
                return;
            }
            if (cmd == IFMCommand.FM_CMD_ABORT_COMPLETE && item == IFMCommand.FM_CMD_SEEK_COMPLETE) {
                cmdList.remove(item);
                return;
            }
        }
    }

    public static boolean checkCmdListComplete(List<Integer> cmdList) {
        return cmdList != null && cmdList.size() == 0;
    }

    public static boolean checkInt(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Integer) {
            return true;
        }

        return false;
    }

    public static boolean checkStr(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return true;
        }

        return false;
    }

    public static boolean checkStatus(int status) {
        if (status == 1 /* XXX */) {
            return true;
        }
        return false;
    }

    public static boolean checkStatusAndInt(int status, Object value) {
        return checkStatus(status) && checkInt(value);
    }

    public static boolean checkStatusAndStr(int status, Object value) {
        return checkStatus(status) && checkStr(value);
    }

    public static char[] decodePI(int pi) {
        char[] rds_pi_str = new char[1];
        if ((pi & 0xf00) == 0) {
            pi = ((pi & 0xf000) >> 4 | 0xa000) | (pi & 0xff);
        } else if ((pi & 0xff) == 0) {
            pi = ((pi & 0xff00) >> 8) | 0xaf00;
        }

        if (pi >= 0x9950 || pi < 0x1000) {
            rds_pi_str[0] = 0;
            if (pi >= 0x9950 && threeLetters.containsKey(pi)) {
                String str = threeLetters.get(pi);
                if (str != null && str.trim().length() == 3) {
                    rds_pi_str = new char[3];
                    char[] strArray = str.toCharArray();
                    rds_pi_str[0] = strArray[0];
                    rds_pi_str[1] = strArray[1];
                    rds_pi_str[2] = strArray[2];
                }
            }
        } else {
            rds_pi_str = new char[4];
            if (pi < 0x54a8) {
                rds_pi_str[0] = 0x4b;
                pi -= 0x1000;
            } else {
                pi -= 0x54a8;
                rds_pi_str[0] = 0x57;
            }
            rds_pi_str[3] = (char) ((pi % 0x1a) + 'A');
            pi = pi / 0x1a;
            rds_pi_str[2] = (char) ((pi % 0x1a) + 'A');
            pi = pi  / 0x1a;
            rds_pi_str[1] = (char) ((pi % 0x1a) + 'A');
        }

        return rds_pi_str;
    }

    public static int getBandByLocale(Locale locale) {
        if (locale == null) {
            return 0;
        }

        String country = locale.getISO3Country();
        if (isBand3(country)) {
            return 3;
        }
        if (isBand2(country)) {
            return 2;
        }
        if (isBand0(country)) {
            return 0;
        }

        return 1;
    }

    public static int getBandForStack(int band) {
        return band == 3 ? 1 : 0;
    }

    public static boolean isBand0(String country) {
        if (country.equals(USA)) {
            return true;
        }
        if (country.equals(KOREA)) {
            return true;
        }
        if (!isLatinCountry(country)) {
            return true;
        }
        return false;
    }

    public static boolean isBand2(String country) {
        if (country.equals(ITALY)) {
            return true;
        }
        if (country.equals(SWITZERLAND)) {
            return true;
        }
        if (country.equals(GREECE)) {
            return true;
        }
        if (country.equals(GERMANY)) {
            return true;
        }
        if (country.equals(THAILAND)) {
            return true;
        }
        if (country.equals(INDONESIA)) {
            return true;
        }
        return false;
    }

    public static boolean isBand3(String country) {
        if (country.equals(JAPAN)) {
            return true;
        }
        return false;
    }

    public static boolean isLatinCountry(String country) {
        for (int i = 0; i < LATIN.length; i++) {
            if (country.equals(LATIN[i])) {
                return true;
            }
        }
        return false;
    }

    public static void sleep(int time) {
        if (time < 0) {
            return;
        }

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}

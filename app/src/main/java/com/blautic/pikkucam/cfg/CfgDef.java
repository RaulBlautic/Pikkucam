package com.blautic.pikkucam.cfg;

/**
 * Created by jsf on 9/07/16.
 */

public class CfgDef {

    public static final int MAX_DEVICES = 2;
    public static final int DEVICE1 = 1;
    public static final int DEVICE2 = 2;

    public static final String SPORT_TENNIS = "tennis";
    public static final String SPORT_PADEL = "padel";

    public static final String PAR_CFG_TEST = "cfg_test0";
    public static final String PAR_VIDEODELETETIME = "cfg_vdtim";
    public static final String VAL_VIDEODELETETIME_DEFAULT = "0"; //0:Nunca 1:12 2:24 3:48 horas
    public static final String PAR_VIDEOQUALITY = "cfg_vqual";
    public static final String VAL_VIDEOQUALITY_DEFAULT = "1";
    public static final String PAR_DRIVEENABLED = "cfg_drvqen";
    public static final String VAL_DRIVEENABLED_DEFAULT = "0";
    public static final String PAR_DRIVEACCOUNT = "cfg_drvacc";
    public static final String VAL_DRIVEACCOUNT_DEFAULT = null;

    public static final String VAL_VIDEODELETETIME_NEVER = "0"; //48 horas
    public static final String VAL_VIDEODELETETIME_12H = "1";
    public static final String VAL_VIDEODELETETIME_24H = "2";
    public static final String VAL_VIDEODELETETIME_48H = "3";

    public static final int VAL_VIDEOQUALITY_LOW = 0;
    public static final int VAL_VIDEOQUALITY_MED = 1;
    public static final int VAL_VIDEOQUALITY_HIGH = 2;
    public static final int VAL_VIDEOQUALITY_MAX = 3;

    public static final String MULTICAMERA_MASTER = "PIKKU_MASTER";

}

package com.blautic.pikkucam.cfg;

import java.util.Date;

/**
 * Created by jsf on 17/07/18.
 */

public class CfgVals {

    public final static int MAX_NUMBER_DEVICES = 4;
    public final static int MAX_NUMBER_SENSORS = 9;
    public final static int NUM_SENSOR_ACX = 0;
    public final static int NUM_SENSOR_ACY= 1;
    public final static int NUM_SENSOR_ACZ= 2;
    public final static int NUM_SENSOR_GYX= 3;
    public final static int NUM_SENSOR_GYY= 4;
    public final static int NUM_SENSOR_GYZ= 5;
    public final static int NUM_SENSOR_MGX= 6;
    public final static int NUM_SENSOR_MGY= 7;
    public final static int NUM_SENSOR_MGZ= 8;

    public final static int DEVICE1 = 1;
    public final static int DEVICE2 = 2;
    public final static int DEVICE3 = 3;
    public final static int DEVICE4 = 4;

    public final static int BASE_PERIOD = 50;
    public final static int CAPTURE_PERIOD = 50;
    public final static int CAPTURE_PERIOD_1 = 50;
    public final static int CAPTURE_PERIOD_2 = 36;
    public final static int CAPTURE_PERIOD_3 = 30;
    public final static int CAPTURE_PERIOD_4 = 26;
    public final static int DIN_PERIOD = 20;
    public final static int GONIO_PERIOD = 200;


    public final static byte BITS_ACEL = 0x07;
    public final static byte ACELX = 0x01;
    public final static byte ACELY = 0x02;
    public final static byte ACELZ = 0x04;

    public final static byte BITS_GYR = 0x38;
    public final static byte GYRX = 0x08;
    public final static byte GYRY = 0x10;
    public final static byte GYRZ = 0x20;

    public final static int BITS_MAGNET = 0x40;
    public final static int MAGNET_OFF = 0x00;
    public final static int MAGNET_ON = 0x40;

    public final static int N_A = 0x00;
    public final static int FREC1= 0x00;
    public final static int FREC2= 0x01;
    public final static int FREC3= 0x02;
    public final static int FREC4= 0x03;

    public final static byte PACKS_PER_INTERVAL = 1;
    public final static byte PACKS_PER_INTERVAL_CAPTURE = 2;

    public static final int CAPTURE_FREE=1;
    public static final int CAPTURE_FIXED=2;
    public static final int CAPTURE_INTERVALS=3;

    public static final int VAL_VIDEOQUALITY_LOW = 0;
    public static final int VAL_VIDEOQUALITY_MED = 1;
    public static final int VAL_VIDEOQUALITY_HIGH = 2;
    public static final int VAL_VIDEOQUALITY_MAX = 3;

    public static final String DEFAULT_GRAPH1_KEY = "GRAPH1";
    public static final int DEFAULT_GRAPH1_VAL = 0;
    public static final String DEFAULT_GRAPH2_KEY = "GRAPH2";
    public static final int DEFAULT_GRAPH2_VAL = 1;
    public static final String DEFAULT_GRAPH3_KEY = "GRAPH3";
    public static final int DEFAULT_GRAPH3_VAL = 2;
    public static final String DEFAULT_GRAPH_VIDEO_KEY = "GRAPHV";

    public static final int SCALE_ACC_4G = 0x08;
    public static final int SCALE_ACC_16G = 0x18;
    public static final int SCALE_ACC_8G = 0x10;
    public static final int SCALE_ACC_2G = 0x00;

    public static final int MIN_ACC = -20;
    public static final int MAX_ACC = 20;
    public static final int MAX_ANGLE = 400;
    public static final int MIN_ANGLE = -400;

    public final static byte CFG_MPU_SNSENABLED_POS = 0;
    public final static byte CFG_MPU_POWM1_POS = 1;
    public final static byte CFG_MPU_SAMPLERATE_POS = 2;
    public final static byte CFG_MPU_ACCELCFG_POS = 3;
    public final static byte CFG_MPU_ACCELCFG2_POS = 4;
    public final static byte CFG_MPU_CONFIG_POS = 5;
    public final static byte CFG_MPU_GYROCFG_POS = 6;


    //bits[4,3]: 00:250dps 01(0x4):500dps 10(0x8):1000dps 11(0xC):2000dps
    public final static int GYR_SCALE_2000 = 0x18;
    public final static int GYR_SCALE_1000 = 0x10;
    public final static int GYR_SCALE_500 = 0x08;
    public final static int GYR_SCALE_250 = 0x00;

    public final static byte GYR_SELECT_2000 = 3;
    public final static byte GYR_SELECT_1000 = 2;
    public final static byte GYR_SELECT_500 = 1;
    public final static byte GYR_SELECT_250 = 0;


    public final static int MPU_FREC_1 = 1;
    public final static int MPU_FREC_5 = 5;
    public final static int MPU_FREC_10 = 10;
    public final static int MPU_FREC_20 = 20;
    public final static int MPU_FREC_30 = 30;
    public final static int MPU_FREC_40 = 40;
    public final static int MPU_FREC_50 = 50;
    public final static int MPU_FREC_100 = 100;
    public final static int MPU_FREC_250 = 250;

    public final static byte MPU_FREC_SELECT_1 = 0;
    public final static byte MPU_FREC_SELECT_5 = 1;
    public final static byte MPU_FREC_SELECT_10 = 2;
    public final static byte MPU_FREC_SELECT_20 = 3;
    public final static byte MPU_FREC_SELECT_30 = 4;
    public final static byte MPU_FREC_SELECT_40 = 5;
    public final static byte MPU_FREC_SELECT_50 = 6;
    public final static byte MPU_FREC_SELECT_100 = 7;
    public final static byte MPU_FREC_SELECT_250 = 8;

}

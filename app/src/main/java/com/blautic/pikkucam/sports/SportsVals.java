package com.blautic.pikkucam.sports;

import com.blautic.pikkucam.sns.SnsCfg;

/**
 * Created by jsf on 10/04/17.
 */

public class SportsVals {

    /******************************* FEATURES ******************************************************************/
    public final static int FEATURE_GESTURE_TYPE_DEFAULT = 0;
    public final static int FEATURE_GESTURE_ACTION_DEFAULT = 0;
    public final static int FEATURE_GESTURE_ACTION_DISABLED = 0;
    public final static int FEATURE_SUBTITLE_TYPE_DEFAULT = 0;
    public final static int FEATURE_SUBTITLE_TYPE_DISABLED = 0;
    public final static int FEATURE_SUBTITLE_TYPE_STANDARD = 1;
    public final static int FEATURE_SLOWMOTION_TYPE_DEFAULT = 0;
    public final static int FEATURE_FILE_NAMES_TYPE_DEFAULT = 0;
    public final static int FEATURE_FILE_NAMES_TYPE_CUSTOM_TEXT = 1;
    public final static int FEATURE_TIME_CONTROL_TYPE_DEFAULT = 0;
    public final static int FEATURE_TIME_PERIOD_NUMBER_DEFAULT = 2;
    public final static int FEATURE_TIME_PERIOD_DURATION_DEFAULT = 20;

    /******************************* IDENTIFICADORES DE DEPORTES ************************************************/
    private static final int SPORTS_NUMBER = 6;
    public static final int TENNIS_ID = 1;
    public static final int PADEL_ID = 2;
    public static final int BASKET_ID = 3;
    public static final int BADMINTON_ID = 4;
    public static final int TABLETENNIS_ID = 5;
    public static final int SQUASH_ID = 6;
    public static final int MULTISPORT_ID = 7;
    public static final int HANDBALL_ID = 8;
    public static final int PELOTA_ID = 9;


    public static final int MULTICAMERA_ID = 90;

    /******************************* IDENTIFICADORES DE DEPORTES ************************************************/
    public final static int PIKKU_POSITION_NO_DEF = -1;
    public final static int PIKKU_POSITION_RIGHT_WRIST = 0;
    public final static int PIKKU_POSITION_LEFT_WRIST = 1;
    public final static int PIKKU_POSITION_WAIST = 2;
    public final static int PIKKU_POSITION_ANKLE = 3;
    public final static int PIKKU_POSITION_FREE = 4;


    /******************************* IDENTIFICADORES DE GOLPES/MOVIMIENTOS ************************************************/
    public final static int DETECTED_NO = 0;
    public final static int DETECTING = 100;

    public static final int OFFSET_STROKE_EVENTS = 10;
    public final static int DETECTED_DRIVE = 11;
    public final static int DETECTED_BACK = 12;
    public final static int DETECTED_SMASH = 13;
    public final static int DETECTED_DRIVE_VOLLEY = 14;
    public final static int DETECTED_BACK_VOLLEY = 15;

    //BADMINTON
    public final static int DETECTED_BADMINTON_CLEAR = 15;

    //BASKET
    public final static int DETECTED_BASKET_SHOT = 16;

    //TENIS
    public final static int DETECTED_TENNIS_SERVICE = 16;

    //HANDBALL
    public final static int DETECTED_HANDBALL_SAVE_ACTION = 16;
    public final static int DETECTED_HANDBALL_SHOT_ACTION = 17;

    //TABLETENIS
    public final static int DETECTED_TABLETENNIS_TOPSPIN_BACK = 16;
    public final static int DETECTED_TABLETENNIS_TOPSPIN_DRIVE = 17;

    //TABLETENIS
    public final static int DETECTED_PELOTA_UP = 16;
    public final static int DETECTED_PELOTA_DOWN = 17;

    public static final int OFFSET_ACTIVITY_EVENTS = 20;
    public final static int DETECTED_STEP = 20;
    public final static int DETECTED_FAST_STEP = 21;
    public final static int DETECTED_JUMP = 22;
    public final static int DETECTED_FOOTUP = 23;
    public final static int DETECTED_BTN = 30;

    /******************************* IDENTIFICADORES DE EVENTOS GENERADOS POR BOTONES O MOVIMIENTOS******************/
    public static final int OFFSET_BTN2_EVENTS = 30;

    //EVENTOS GENERADOS CON PULSACIONES DE BOTONES
    public final static int EVT_BTN_N_A = 100;
    public final static int EVT_BTN_VID_START = 31;
    public final static int EVT_BTN_VID_FINISH = 32;
    public final static int EVT_BTN_VID_SAVED= 33;         //Videos salvados
    public final static int EVT_VID_ORDER = 34;            //Motivos de videos
    public final static int EVT_VID_POINT = 35;
    public final static int EVT_VID_AUTO_PAR_DOUBLE = 36;
    public final static int EVT_VID_AUTO_PAR_NET = 37;
    public final static int EVT_VID_AUTO_PAR_SERVICE = 38;
    public final static int EVT_VID_AUTO_PAR_ERROR = 39;
    public final static int EVT_VID_AUTO_PAR_REBOUND = 40;
    public final static int EVT_VID_AUTO_PAR_STEAL = 41;
    public final static int EVT_VID_AUTO_PAR_ASSIST = 42;
    public final static int EVT_VID_AUTO_PAR_TURNOVER = 43;
    public final static int EVT_VID_AUTO_PAR_BLOCK = 44;

    public final static int EVT_VID_AUTO_SMASH = 45;
    public final static int EVT_VID_AUTO_SHOT = 46;
    public final static int EVT_VID_AUTO_SAVE_ACTION = 48;

    public final static int EVT_VID_AUTO_PAR_GENERIC = 47;

    public final static int EVT_BTN_PAR_DOUBLE = 60;
    public final static int EVT_BTN_PAR_UNFORCED = 61;
    public final static int EVT_BTN_PAR_1STSERVE = 62;
    public final static int EVT_BTN_PAR_NET = 63;
    public final static int EVT_BTN_PAR_REBOUND = 64;
    public final static int EVT_BTN_PAR_STEAL = 65;
    public final static int EVT_BTN_PAR_ASSIST = 66;
    public final static int EVT_BTN_PAR_TURNOVER = 67;
    public final static int EVT_BTN_PAR_BLOCK = 68;
    //MULTI
    public final static int EVT_BTN_PAR_GENERIC = 69;
    public final static int EVT_BTN_PAR_WIN = 70;

    //EVENTOS GENERADOS CON MOVIMIENTOS/GOLPES
    // public final static int EVT_AUTO_SMASH = 50;
    // public final static int EVT_AUTO_PAR = 51;  //Genera video cuando activamos parámetro

    /******************************* IDENTIFICADORES DE MARCADOR ************************************************/
    public static final int EVT_TYPE_SCORE_ERROR = -1;
    public static final int EVT_TYPE_SCORE_START = 0;
    public static final int EVT_TYPE_SCORE_POINT = 1;
    public static final int EVT_TYPE_SCORE_BREAKPOINT = 2;
    public static final int EVT_TYPE_SCORE_GAMEAFTERBREAKPOINT = 3;
    public static final int EVT_TYPE_SCORE_GAME= 4;
    public static final int EVT_TYPE_SCORE_SETAFTERBREAKPOINT = 5;
    public static final int EVT_TYPE_SCORE_SET = 6;
    public static final int EVT_TYPE_SCORE_MATCHPOINT = 7;
    public static final int EVT_TYPE_SCORE_MATCH = 8;
    public static final int EVT_TYPE_SCORE_BACK = 9;
    public static final int EVT_TYPE_SCORE_GOTSERVICE = 10;
    public static final int EVT_TYPE_SCORE_FINISH = 99;

    public static final int EVT_TYPE_SESSION_START = 0;
    public static final int EVT_TYPE_SESSION_FINISH = 99;

    /******************************* CONFIGURACIONES DE CONTROL DE MOVIMIENTOS ************************************************/
    public static final int EVT_CFG_TYPE_RAQUET_STROKES = 0;
    public static final int EVT_CFG_TYPE_ACTIVITY_STEPS = 1;


    public final static int GESTURE_WRIST_SPIN = 0;
    public final static int GESTURE_TIMEGAP = 3000;

    public final static byte[] DEFAULT_CFGMPU = new byte[]{
            0,
            SnsCfg.MPU_MODE_ENABLED_EVT,   //TENNIS_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //PADEL_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //BASKET_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //BADMINTON_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //TABLETENNIS_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //SQUASH_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //MULTISPORT_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //HANDBALL_ID
            SnsCfg.MPU_MODE_ENABLED_RAW   //PELOTA_ID
    };


    public final static float[][] DEFAULT_EVT_PROMED = new float[][]{
            {0,0,0,0,0,0},
            {-3.7f,1.9f,0.5f,0.1f,0.0f,-0.1f},  //TENNIS_ID
            {-3.14f,2.76f,0.98f,0.01f,0.04f,-0.21f},  //PADEL_ID
            {-3.01f,2.08f,0.51f,0.2f,0.42f,0.28f},  //BASKET_ID
            {-4.05f,1.01f,2.00f,-0.2f,0.12f,-0.05f},  //BADMINTON_ID
            {-3.7f,3.5f,0.3f,0.4f,0.1f,-0.1f},  //TABLETENNIS_ID
            {-5.1f,1.9f,1.8f,0.4f,-0.1f,-0.1f},   //SQUASH_ID
            {-5.1f,-1.9f,-1.8f,0.4f,0.1f,0.1f},   //MULTISPORT_ID
            {-3.1f,2.0f,1.4f,0.1f,0.1f,-0.1f},   //HANDBALL_ID
            {-3.4f,2.3f,1.5f,0.0f,-0.1f,-0.3f}   //PELOTA_ID

    };

    public final static float[][] DEFAULT_EVT_DESVEST = new float[][]{
            {0,0,0,0,0,0},
            {3.6f,3.2f,3.2f,2.4f,2.3f,2.6f},  //TENNIS_ID
            {3.5f,3.02f,3.24f,0.20f,2.46f,2.34f},  //PADEL_ID
            {4.74f,4.48f,4.07f,3.54f,2.34f,2.93f},  //BASKET_ID
            {4.21f,1.0f,1.50f,3.64f,2.96f,3.11f},  //BADMINTON_ID
            {4.3f,4.0f,3.8f,2.8f,2.9f,3.6f},  //TABLETENNIS_ID
            {4.2f,3.7f,3.6f,4.1f,3.6f,4.3f},   //SQUASH_ID
            {4.2f,3.7f,3.6f,4.1f,3.6f,4.3f},   //MULTISPORT_ID
            {3.4f,3.4f,3.2f,2.3f,1.8f,2.3f},   //HANDBALL_ID
            {3.6f,3.4f,3.3f,2.6f,2.2f,3.1f}   //PELOTA_ID
    };

    public final static float[] DEFAULT_EVT_K = new float[]{
            0,
            1.8f,   //TENNIS_ID
            2.15f,   //PADEL_ID
            1.8f,   //BASKET_ID
            2.5f,   //BADMINTON_ID
            1f,   //TABLETENNIS_ID
            2.0f,   //SQUASH_ID
            2.0f,   //MULTISPORT_ID
            3.75f,   //HANDBALL_ID
            3f   //PELOTA_ID
    };

    public final static int[] DEFAULT_EVT_UMBRAL = new int[]{
            0,
            2,   //TENNIS_ID
            2,   //PADEL_ID
            1,   //BASKET_ID
            2,   //BADMINTON_ID
            1,   //TABLETENNIS_ID
            2,   //SQUASH_ID
            2,   //MULTISPORT_ID
            4,   //HANDBALL_ID
            4   //PELOTA_ID
    };

    public final static byte[] DEFAULT_EVT_NSAMPLES = new byte[]{
            0,
            22,   //TENNIS_ID
            20,   //PADEL_ID
            20,   //BASKET_ID
            20,   //BADMINTON_ID
            13,   //TABLETENNIS_ID
            20,   //SQUASH_ID
            20,   //MULTISPORT_ID
            20,   //HANDBALL_ID
            20   //PELOTA_ID
    };

    public final static byte[] ACTIVITY_CFGMPU = new byte[]{
            0,
            SnsCfg.MPU_MODE_ENABLED_RAW,   //TENNIS_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //PADEL_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //BASKET_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //BADMINTON_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //TABLETENNIS_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //SQUASH_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //MULTISPORT_ID
            SnsCfg.MPU_MODE_ENABLED_RAW,   //HANDBALL_ID
            SnsCfg.MPU_MODE_ENABLED_RAW   //PELOTA_ID
    };


    public final static float[][] ACTIVITY_EVT_PROMED = new float[][]{
            {0,0,0,0,0,0},
            {-0.9f,3.5f,0.2f,0.0f,0.0f,0.0f},  //TENNIS_ID
            {-3.14f,-2.76f,-0.98f,0.01f,-0.04f,0.21f},  //PADEL_ID
            {-3.01f,-2.08f,-0.51f,0.2f,-0.42f,-0.28f},  //BASKET_ID
            {-4.05f,-1.01f,-2.00f,-0.2f,-0.12f,0.05f},  //BADMINTON_ID
            {-3.7f,-3.5f,-0.3f,0.4f,-0.1f,0.1f},  //TABLETENNIS_ID
            {-5.1f,-1.9f,-1.8f,0.4f,0.1f,0.1f},   //SQUASH_ID
            {-5.1f,-1.9f,-1.8f,0.4f,0.1f,0.1f},   //MULTISPORT_ID
            {-3.01f,-2.08f,-0.51f,0.2f,-0.42f,-0.28f},   //HANDBALL_ID
            {-3.01f,-2.08f,-0.51f,0.2f,-0.42f,-0.28f},   //PELOTA_ID
    };

    public final static float[][] ACTIVITY_EVT_DESVEST = new float[][]{
            {0,0,0,0,0,0},
            {1.2f,1.6f,1.0f,0.5f,1.2f,0.3f},  //TENNIS_ID
            {3.5f,3.02f,3.24f,0.20f,2.46f,2.34f},  //PADEL_ID
            {4.74f,4.48f,4.07f,3.54f,2.34f,2.93f},  //BASKET_ID
            {4.21f,1.0f,1.50f,3.64f,2.96f,3.11f},  //BADMINTON_ID
            {4.3f,4.0f,3.8f,2.8f,2.9f,3.6f},  //TABLETENNIS_ID
            {4.2f,3.7f,3.6f,4.1f,3.6f,4.3f},   //SQUASH_ID
            {4.2f,3.7f,3.6f,4.1f,3.6f,4.3f},   //MULTISPORT_ID
            {4.74f,4.48f,4.07f,3.54f,2.34f,2.93f},   //HANDBALL_ID
            {4.74f,4.48f,4.07f,3.54f,2.34f,2.93f},   //PELOTA_ID
    };

    public final static float[] ACTIVITY_EVT_K = new float[]{
            0,
            0.3f,   //TENNIS_ID
            2.15f,   //PADEL_ID
            2.5f,   //BASKET_ID
            2.5f,   //BADMINTON_ID
            1f,   //TABLETENNIS_ID
            2.0f,   //SQUASH_ID
            2.0f,   //MULTISPORT_ID
            2.5f,   //HANDBALL_ID
            2.5f   //PELOTA_ID
    };

    public final static int[] ACTIVITY_EVT_UMBRAL = new int[]{
            0,
            4,   //TENNIS_ID
            2,   //PADEL_ID
            1,   //BASKET_ID
            2,   //BADMINTON_ID
            1,   //TABLETENNIS_ID
            2,   //SQUASH_ID
            2,   //MULTISPORT_ID
            1,   //HANDBALL_ID
            1   //PELOTA_ID
    };

    public final static byte[] ACTIVITY_EVT_NSAMPLES = new byte[]{
            0,
            35,   //TENNIS_ID
            20,   //PADEL_ID
            20,   //BASKET_ID
            20,   //BADMINTON_ID
            13,   //TABLETENNIS_ID
            20,   //SQUASH_ID
            20,   //MULTISPORT_ID
            20,   //HANDBALL_ID
            20   //PELOTA_ID
    };

    //CAMBIOS DE SIGNO PULSERA PARTE INFERIOR A SUPERIOR
    /*
    DIESTRO

            x             =
            y             cambia signo
            z              cambia signo

     ZURDO
            x             =
            y             cambia signo
            z             cambia signo
    */

    /* Información almacenadas en el perfil coach*/
    /*
        coachProfile.addStringProperty("name_prf");
        coachProfile.addBooleanProperty("enable_sound");
        coachProfile.addBooleanProperty("enable_activity");
        coachProfile.addBooleanProperty("enable_strokes");
        coachProfile.addBooleanProperty("enable_autovideos");
        coachProfile.addBooleanProperty("enable_videos");
        coachProfile.addBooleanProperty("enable_parameters");
        coachProfile.addIntProperty("number_of_devices");
        coachProfile.addIntProperty("first_btn_short_dev1");  //Función botón 1: salvar video o iniciar video (tipo de video manual;
        coachProfile.addIntProperty("first_btn_short_dev2"); //Función botón 1: salvar video o iniciar video (tipo de video manual;
        coachProfile.addIntProperty("first_btn_long_dev1");
        coachProfile.addIntProperty("first_btn_long_dev2");
        coachProfile.addIntProperty("second_btn_short_dev1"); //Función botón 2: salvar video (video manual) o guardar parámetro
        coachProfile.addIntProperty("second_btn_short_dev2");  /Función botón 2: salvar video (video manual) o guardar parámetro
        coachProfile.addIntProperty("second_btn_long_dev1");
        coachProfile.addIntProperty("second_btn_long_dev2");
        coachProfile.addIntProperty("position_dev1");
        coachProfile.addIntProperty("position_dev2");
        coachProfile.addIntProperty("video_type");          //Tipo de video:Continuo,actividad,golpes o manual
        coachProfile.addIntProperty("auto_video_type1");    //Tipo de auto:
        coachProfile.addIntProperty("auto_video_type2");
        coachProfile.addIntProperty("video_time");
        coachProfile.addIntProperty("auto_video_time1");
        coachProfile.addIntProperty("auto_video_time2");
        coachProfile.addIntProperty("video_camera");
        coachProfile.addIntProperty("video_action");
        coachProfile.addDateProperty("last_change");
        coachProfile.addLongProperty("sportId");
     */


}

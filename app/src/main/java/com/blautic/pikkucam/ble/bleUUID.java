package com.blautic.pikkucam.ble;

public class bleUUID {
	// Aqui almacenamos todos los UUID programados para modificarlos de una manera sencilla
	//public static final int UUID_ECG = 0;
	//public final static String[] UUID_SERVICES = {"ff00"};

    /*

	#define CFG_MPU_LEN             7
//[EnableSensors][PowerMgmt1][SampleRateDivider][AccelCfg][AccelCfg2][Config][GyroCfg]

//EnableSensors (reg PowerManagement2): bit0:accel bit1:gyro bit2:magnet

1: habilitado
0: deshabilitado

			#define CFG_MPU_SENSORSENABLED        0
            #define CFG_MPU_POWERMGMT1            1
            #define CFG_MPU_SAMPLERATE            2
            #define CFG_MPU_ACCELCFG              3
            #define CFG_MPU_ACCELCFG2             4
            #define CFG_MPU_CONFIG                5
            #define CFG_MPU_GYROCFG               6


Sensores
    Acel: bit2:z bit1:y bit0:x
    Gyro: bit5:z bit4:y bit3:x
    Magnet: bit6:completo

    Trama por defecto: habilitados acelerómetro,giroscopio y magnetómetro
            order = new byte[]{0x07F,0x00,0x00,0x08,0x03,0x03,0x10};
*/


	public final static String aux = "0000XXXX-0000-1000-8000-00805f9b34fb";

	public final static String UUID_SERVICE = "ff30";

	public final static String UUID_SCORE_FW_VERS = "ff31";


    public final static String UUID_PIKKU_FW_VERS = "ff31";
    public final static String UUID_PIKKU_GROUP = "ff32";
    public final static String UUID_PIKKU_MATCH = "ff33";
    public final static String UUID_PIKKU_TEAM = "ff34";
    public final static String UUID_PIKKU_OPER = "ff35";
    public final static String UUID_PIKKU_POW = "ff36";
    public final static String UUID_PIKKU_BTN1 = "ff37";
    public final static String UUID_PIKKU_DATAMPU = "ff38"; /* DATOS MPU*/
    public final static String UUID_PIKKU_BTN2 = "ff39";
    public final static String UUID_PIKKU_CFG = "ff3a";
    public final static String UUID_PIKKU_FREC = "ff3b";     /* FRECUENCIA DE ENVÍO DE DATOS DEL SENSOR */
    public final static String UUID_PIKKU_CFGMPU= "ff3c";     /* CONFIGURACIÓN PARÁMETROS DEL SENSOR */
    public final static String UUID_PIKKU_UNIX= "ff3d";     /* CONFIGURACIÓN PARÁMETROS DEL SENSOR */

	public final static byte INT_SCORE_FW_VERS = 0x31;

	public final static byte INT_DEVICE_BTN = 0x34;
	public final static byte INT_SCORE_OPER = 0x35;

	public final static byte INT_SCORE_FREC = 0x3b;     /* FRECUENCIA DE ENVÍO DE DATOS DEL SENSOR */
	public final static byte INT_SCORE_CFGMPU= 0x3c;     /* CONFIGURACIÓN PARÁMETROS DEL SENSOR */
    public final static byte INT_SCORE_CFGLIS= 0x3d;     /* CONFIGURACIÓN PARÁMETROS DEL EVENTO DE ACTIVIDAD Y RECEPCIÓN DE DATOS DEL EVT */
	
	public final static byte OPER_IDLE = 0;
	public final static byte OPER_ACEL_RAW = 1;
	public final static byte OPER_AI = 2;
	public final static byte OPER_CALIBRATE = 3;
    public final static byte OPER_DOWNLOAD = 4;
    public final static byte OPER_DELETE_MEMORY = 5;
    public final static byte OPER_SWITCH_OFF = 10;

	public final static byte TAG_ID_DEVICE = (byte) 0xBF;
	public final static int  POS_ID_DEVICE = 5;


	// PIKKU SPORT 5
	public final static byte TAG_ID_BL5 = (byte) 0xBE;
	public final static byte INT_FRAME_ID_SCORE_BL5 = 0x33;


	// PIKKU LAB 5
	public final static byte TAG_ID_LAB_BL5 = (byte) 0xBE;
	public final static byte INT_FRAME_ID_SCORE_LAB_BL5 = 0x22;

	// PIKKU ACADEMY
	public final static byte INT_FRAME_ID_SCORE_PIKKU_ACADEMY= 0x55;


	public final static byte INT_FRAME_ID_SCORE = 0x22;
	public final static int POS_FRAME_ID = 6;
	public final static int POS_VERSION = 7;


	public final static int POS_TAG_GROUP = 9;
	public final static int POS_TAG_MATCH = 12;
	public final static int POS_TAG_BTN = 16;
	public final static int POS_TAG_BTN1 = 17;
	public final static int POS_TAG_BTN2 = 18;


	public final static int TAM_BROADCAST = 20;


    

}
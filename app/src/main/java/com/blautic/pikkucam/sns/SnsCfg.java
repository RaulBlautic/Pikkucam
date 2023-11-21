package com.blautic.pikkucam.sns;

import android.util.Log;

import com.blautic.pikkucam.ble.bleUUID;
import com.blautic.pikkucam.cfg.CfgOrder;
import com.blautic.pikkucam.cfg.CfgVals;

import java.util.ArrayList;

public class SnsCfg {

    public static final int  ORDER_MODE = 1;
    public static final int  ORDER_EVT = 2;
    public static final int  ORDER_CFG_MPU = 3;

    public int DEFAULT_PERIOD = CfgVals.BASE_PERIOD;

    public static final byte MPU_MODE_DISABLED = 0;
    public static final byte MPU_MODE_ENABLED_RAW = 1;
    public static final byte MPU_MODE_ENABLED_EVT = 2;

    private static final int CFG_SIZE = 15;
    private static final int CA2_MAX = 256;
    private static final int CA2_MED = 127;

    public ArrayList<CfgOrder> orderList = new ArrayList();
    public ArrayList<CfgOrder> orderPermanent = new ArrayList();

    //Cfg por defecto
    byte mpuMode=MPU_MODE_DISABLED;
    byte acelCfg = CfgVals.BITS_ACEL;
    byte gyroCfg = CfgVals.BITS_GYR;
    byte magnetCfg = CfgVals.BITS_MAGNET;
    byte sensorsCfg=0;
    byte acelScale=CfgVals.SCALE_ACC_4G;
    byte gyroScale=CfgVals.GYR_SCALE_1000;
    byte pktsPerInterval = CfgVals.PACKS_PER_INTERVAL;
    byte lastPktsPerInterval = CfgVals.PACKS_PER_INTERVAL;;

    byte session_best_period = (byte) DEFAULT_PERIOD;
    byte session_last_period = (byte) DEFAULT_PERIOD;


    public SnsCfg() {

        createOrders();
    }

    public void setSensorsCfg(byte acel,byte gyro,byte magnet,byte scaleAcc,byte scaleGyro,int period,byte pkts) {

        acelCfg=acel;
        gyroCfg=gyro;
        magnetCfg=magnet;
        sensorsCfg=(byte)(magnetCfg | gyroCfg | acelCfg);
        acelScale=scaleAcc;
        gyroScale=scaleGyro;

        DEFAULT_PERIOD=period;
        session_best_period = (byte) DEFAULT_PERIOD;
        session_last_period = (byte) DEFAULT_PERIOD;

        session_best_period=(byte) getBestPeriod();

        pktsPerInterval=pkts;
    }

    public byte[] getMpuMode()
    {
        byte[] cfg = new byte[]{mpuMode};
       // byte[] cfg = new byte[]{mpuMode,3}; //prueba de enviar tiempo
        return cfg;
    }

    public  void setMpuMode(byte mode)
    {
        mpuMode=mode;
    }


    public  void createOrders()
    {

         orderList.clear();
         CfgOrder cfgOrder;// = new CfgOrder(ORDER_MODE);

        //Configuramos mpu si está habilitada
      //  if(session_best_period != session_last_period)
        {
            cfgOrder = new CfgOrder(ORDER_CFG_MPU);
            cfgOrder.setUuid(bleUUID.UUID_PIKKU_FREC);
           //Con numero de tramas por intervalo cfgOrder.setOrder(new byte[]{session_best_period,3});
            if(pktsPerInterval != lastPktsPerInterval) cfgOrder.setOrder(new byte[]{session_best_period,pktsPerInterval});
            else cfgOrder.setOrder(new byte[]{session_best_period});

            lastPktsPerInterval=pktsPerInterval;
            orderList.add(cfgOrder);
            orderPermanent.add(cfgOrder);
            session_last_period=session_best_period;
        }

        if(mpuMode == MPU_MODE_ENABLED_RAW)
        {
            cfgOrder = new CfgOrder(ORDER_CFG_MPU);
            cfgOrder.setUuid(bleUUID.UUID_PIKKU_CFGMPU);
            cfgOrder.setOrder(getCfgMpu());
            orderList.add(cfgOrder);
            orderPermanent.add(cfgOrder);
        }

        //Habilitamos o deshabilitamos mpu
        cfgOrder = new CfgOrder(ORDER_MODE);
        cfgOrder.setUuid(bleUUID.UUID_PIKKU_OPER);
        cfgOrder.setOrder(getMpuMode());
        orderList.add(cfgOrder);
        orderPermanent.add(cfgOrder);

        Log.d("ORDER","ORDER--->"+mpuMode+" lista:"+orderList.size());


    }



    public byte CA2(float f)
    {
        int val = (int)(f*10.0);
        if(val >= CA2_MAX) val = CA2_MAX;
        if(val > CA2_MED) val = CA2_MAX - val;
        return (byte)val;
    }

    public boolean isAnyOrderPending()
    {
        Log.d("ORDER","CHECK ORDERS --->"+orderList.size());
        if(orderList.size() == 0) return false;
        else return true;
    }

    public CfgOrder getNextOrder()
    {
        if(isAnyOrderPending()) {
           return orderList.get(0);
        }

        return null;
    }

    public void setOrderResult(boolean result)
    {
        if(result && orderList.size()>0) orderList.remove(0);
    }

    public void restoreList()
    {
        if(orderList.size()==0) {
            for (CfgOrder c : orderPermanent) {
                orderList.add(c);
            }
        }
    }

    private byte[] getCfgMpu()
    {
        byte[] order;

        sensorsCfg=(byte)(magnetCfg | gyroCfg | acelCfg);

       // order = new byte[]{sensorsCfg,0x00,0x00,acelScale,0x03,0x03,0x10};
        order = new byte[]{sensorsCfg,0x00,0x00,acelScale,0x03,0x03,gyroScale};

        return order;
    }

    public byte getSensorsCfg()
    {

        return sensorsCfg;
    }

    public int getBestPeriod()
    {
        int best=DEFAULT_PERIOD;

        int modoCaptura=0;
        int differentSensors=0;
        int numSensorPacks=0;
        byte cfg= sensorsCfg;//snsCfg.getSensorsCfg();
        int bit = 0x0001;

        differentSensors=0;

        if((cfg & CfgVals.BITS_MAGNET) >0)
        {
            //magnet habilitado... ponemos a 1 3 bits
            modoCaptura = ((cfg | 0x0180) & 0x0FFF);
        }else modoCaptura = sensorsCfg;

        for(int i=0;i<9;i++) {

            if (((modoCaptura & bit) & 0x0FFF) > 0) differentSensors++;
            bit <<= 1;
        }

        numSensorPacks = CfgVals.MAX_NUMBER_SENSORS/differentSensors;
        best = DEFAULT_PERIOD/numSensorPacks;

        Log.d("CFG","Best Period ---> "+best);

        return best;
    }



    public int getFrec()
    {

        switch(session_best_period)
        {
            case 2: case 3:
                return 600;

            case 4: case 5:
                return 400;

            case 8:
                return 200;

            case 10:
                return 180;

            case 12:
                return 150;

            case 30:
                return 60;

            case 26:
                return 70;

            case 36:
                return 50;

            default:
                return (1000/session_best_period)*2; //2=numero pkts per tx

        }

    }


}

package com.blautic.pikkucam.sns;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blautic.pikkucam.cfg.CfgVals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorDevice {

    String TAG = "SensorDevice";

    public static final String BLE_NOTIFY = "BTCSCORE_BLE_NOTIFY";

    public double RATIO_ACC = (4.0/32767);
    public double RATIO_GYRO = (1000.0/32767);
    public double RATIO_MAG = (48.0/32767);

    public float[] valor = new float[CfgVals.MAX_NUMBER_SENSORS];

    private int ndevice;

    private int modoCaptura=0;
    private int valuesXFrame=0;
    private int differentSensors=0;
    private int samplesXFrame=0;
    private int diffSensorsXFrame=0;
    private int[] snsType = new int[CfgVals.MAX_NUMBER_SENSORS];
    public boolean sampleReset = false;

    public SnsCfg snsCfg = new SnsCfg();

    //Almacenaje de samples:
    public boolean storeInSampleList=false;
    public int last_nsample=0;
    public List<Sample> sampleList = new ArrayList<Sample>();

    Context _context;

    public int ncurrentsample;

    private int errorSamples=0;

    public SensorDevice(int num){
        ndevice = num;
    }

    public void receiveData(byte[] d)
    {
        /* float acx,acy,acz,gyx,gyy,gyz,mgx,mgy,mgz=0;*/
        int n_sample=0;
        int n_sensors_x_sample=0;
        int n_sample_this_frame=1;
        n_sample = ((d[1] & 0xFF) << 8 | d[0] & 0xFF);

        //Log.i(TAG,"Rxdata("+ndevice+") s:"+n_sample+ " last:"+last_nsample);
        if(n_sample != last_nsample+1){
            errorSamples++;
            Log.i(TAG,"**** "+ ndevice+" vxf:"+valuesXFrame+" sxf:"+samplesXFrame+" dsxf:"+diffSensorsXFrame+" s:"+n_sample+ " last:"+last_nsample+"****");
        }

        Sample s = new Sample(n_sample);//,unixTime);
        //Log.i("Device "+ndevice+":", "Sample n: " + n_sample);
        for(int i=0;i<valuesXFrame;i++)
        {
            valor[i] = (float) ((d[2*i+1+2] & 0xFF) << 8 | d[2*i+2] & 0xFF);
            if (valor[i] > 32767) valor[i] = -(65534 - valor[i]);
            valor[i] = convert(i, valor[i]);


            n_sensors_x_sample++;

            if(storeInSampleList && n_sample >= last_nsample)
            {
                s.setValor(snsType[i]/3,(snsType[i]%3),valor[i]);

                if(n_sensors_x_sample >= diffSensorsXFrame)
                {
                    // Log.i(TAG,"Rxdata new pack s:"+n_sample+" nspack:"+n_sensors_x_sample+" nstf:"+n_sample_this_frame);
                    n_sensors_x_sample=0;
                    sampleList.add(s);

                    if(n_sample_this_frame<samplesXFrame){//samplesXFrame>1){
                        n_sample++;
                        n_sample_this_frame++;
                        s=new Sample(n_sample);
                    }

                }

            }else
            {
                if(n_sensors_x_sample >= diffSensorsXFrame) {
                    //Log.i(TAG,"NoStore -- Rxdata new pack s:"+n_sample+" nspack:"+n_sensors_x_sample+" nstf:"+n_sample_this_frame);
                    n_sensors_x_sample = 0;
                    if(n_sample_this_frame<samplesXFrame) {//samplesXFrame>1)
                        n_sample++;
                        n_sample_this_frame++;

                    }

                }
            }

        }

        Intent intent = new Intent(BLE_NOTIFY);
        valor[0] = valor[0]-0.25f;
        intent.putExtra("source",1);
        intent.putExtra("data", valor);
        // Log.w("BLEREM"," BACK/DRIVE ----->"+valor);
        LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);

        last_nsample=n_sample;
        ncurrentsample=n_sample;
    }


    private float convert(int idx, float val){
        switch (idx)
        {
            case CfgVals.NUM_SENSOR_ACX:
                //filter_x=(float) lowpassA.filter(val*RATIO_ACC);
                //return filter_x;
                return (float)(val*RATIO_ACC);

            case CfgVals.NUM_SENSOR_ACY:
                //filter_y=(float) lowpassB.filter(val*RATIO_ACC);
                //return filter_y;
                return (float)(val*RATIO_ACC);

            case CfgVals.NUM_SENSOR_ACZ:
                //filter_z=(float) lowpassC.filter(val*RATIO_ACC);
                //return filter_z;
                return (float)(val*RATIO_ACC);

            case CfgVals.NUM_SENSOR_GYX: case CfgVals.NUM_SENSOR_GYY: case CfgVals.NUM_SENSOR_GYZ:
            return (float)(val*RATIO_GYRO);

            case CfgVals.NUM_SENSOR_MGX: case CfgVals.NUM_SENSOR_MGY: case CfgVals.NUM_SENSOR_MGZ:
            float valC;
            val *= RATIO_MAG;
            return val;
        }
        return 0;
    }

    public void setModoCaptura()
    {
        byte cfg= snsCfg.getSensorsCfg();
        int bit = 0x0001;
        differentSensors=0;

        if((cfg & CfgVals.BITS_MAGNET) >0)
        {
            //magnet habilitado... ponemos a 1 3 bits
            modoCaptura = ((cfg | 0x0180) & 0x0FFF);
        }else modoCaptura = snsCfg.getSensorsCfg();

        for(int i=0;i<9;i++) {

            if (((modoCaptura & bit) & 0x0FFF) > 0) differentSensors++;
            bit <<= 1;
        }

        samplesXFrame = CfgVals.MAX_NUMBER_SENSORS/differentSensors;
        diffSensorsXFrame = differentSensors;///samplesXFrame;//CfgVals.MAX_NUMBER_SENSORS/samplesXFrame;
        valuesXFrame = samplesXFrame*diffSensorsXFrame;

        //Combinaciones posibles
        //Si solo permitimos sueltos en 1 tipo.
        // Solo acelerómetro
        //Magnet on Gyro on 1111XXX     De 120 a 127
        //Magnet on Gyro off 1000XXX    De 64 a 71
        //Magnet off Gyro on 0111XXX    De 56 a 63
        //Magnet off Gyro off 0000XXX   De 0 a 7

        setReceiveSnsTypes();
        last_nsample=0;

    }

    private void setReceiveSnsTypes()
    {
        int idx = 0;
        int detected =0;
        int bit = 0x0001;

        for(int datapos=0;datapos<9;datapos++)
        {
            if(detected >= differentSensors){

                idx=0;
                detected=0;
            }

            bit=0x0001;
            bit <<= idx;

            for (int snstype = idx; snstype < 9; snstype++)
            {
                if (((modoCaptura & bit) & 0x0FFF) > 0) {
                    detected++;
                    snsType[datapos] = snstype;
                    idx=snstype+1;
                    break;
                }
                bit <<= 1;
            }
        }

        String conc="";
        for(int value : snsType)
            conc=conc.concat(" "+ value);

        Log.i("CFG *****",conc);

    }

    public void setScale()
    {
        switch(snsCfg.acelScale)
        {
            case CfgVals.SCALE_ACC_2G:
                RATIO_ACC = (2.0/32767);
                break;

            case CfgVals.SCALE_ACC_4G:
                RATIO_ACC = (4.0/32767);
                break;

            case CfgVals.SCALE_ACC_8G:
                RATIO_ACC = (8.0/32767);
                break;

            case CfgVals.SCALE_ACC_16G:
                RATIO_ACC = (16.0/32767);
                break;
        }

        switch(snsCfg.gyroScale)
        {
            case CfgVals.GYR_SCALE_250:
                RATIO_GYRO =(250.0/32767);
                break;

            case CfgVals.GYR_SCALE_500:
                RATIO_GYRO =(500.0/32767);
                break;

            case CfgVals.GYR_SCALE_1000:
                RATIO_GYRO =(1000.0/32767);
                break;

            case CfgVals.GYR_SCALE_2000:
                RATIO_GYRO =(2000.0/32767);
                break;
        }


    }

    public void setContext(Context context){
        this._context = context;
    }

    public float getValor(int index) {
        return valor[index];
    }

    public int getNdevice() {
        return ndevice;
    }
}
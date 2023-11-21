package com.blautic.pikkucam.sns;

public class Sample {

    public int nsample;
    float[][] valor = new float[][]{{0f,0f,0f},{0f,0f,0f},{0f,0f,0f}};
    public long ms=0;

    public Sample(int nsample){
        super();
        this.nsample = nsample;
        ms=0;
    }

    public void setValor(int sns, float x, float y, float z){

        valor[sns][0] = x;
        valor[sns][1] = y;
        valor[sns][2] = z;

    }

    public void setValor(int sns, int axis, float val){
        valor[sns][axis]=val;
    }

    public float getValor(int sns, int eje) {
        return valor[sns][eje];
    }
}

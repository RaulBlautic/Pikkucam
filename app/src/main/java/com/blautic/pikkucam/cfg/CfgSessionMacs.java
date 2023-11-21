package com.blautic.pikkucam.cfg;

/**
 * Created by jsf on 17/07/18.
 */

public class CfgSessionMacs
{
    public int numMacsSession=0;

    private String[] macs = new String[CfgVals.MAX_NUMBER_DEVICES+1];
    private boolean[] macs_enabled = new boolean[CfgVals.MAX_NUMBER_DEVICES+1];
    private boolean[] ble5 = new boolean[CfgVals.MAX_NUMBER_DEVICES+1];

    public boolean isMacEnabled(int which)
    {
        return macs_enabled[which];
    }

    public void setMacEnabled(int which, boolean what)
    {
        macs_enabled[which]=what;
    }

    public boolean isBle5(int which)
    {
        return ble5[which];
    }

    public void setBle5(int which, boolean what)
    {
        ble5[which]=what;
    }

    public String getMac(int which) {
        return macs[which];
    }

    public void setMac(int which,String mac) {
        this.macs[which] = mac;
    }

    public int getNumMacsSession() {
        return numMacsSession;
    }

    public void setNumMacsSession(int numMacsSession) {
        this.numMacsSession = numMacsSession;
    }
}

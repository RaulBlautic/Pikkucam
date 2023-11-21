package com.blautic.pikkucam.video;

/**
 * Created by jsf on 28/04/19.
 */

public class MulticameraItem {

    private static final int MAX_MULTI_DEVICES=4;

    private int maxItems;
    //private String basePath;
    private int numItem;
    private boolean slowmotion=false;
    private int rxItems;
    private boolean[] rxStatus = new boolean[]{false,false,false,false};
    private boolean ready=false;
    private String[] pathItems = new String[MAX_MULTI_DEVICES];
    private String output;


    private boolean finished=false;

    public MulticameraItem(int num,int max,boolean sm) {
        //this.basePath = basePath;
        //pathItems[num]=path;
        this.numItem = num;
        maxItems=max;
        slowmotion=sm;
    }

    public boolean isSlowmotion() {
        return slowmotion;
    }

    /*public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
*/
    public void setPathItem(int which,String path)
    {
        pathItems[which]=path;
    }

    public String getPathItem(int which)
    {
        return pathItems[which];
    }

    public int getNumItem() {
        return numItem;
    }

    public void setOrder(int numItem) {
        this.numItem = numItem;
    }

    public int getRxItems() {
        return rxItems;
    }

    public void setRxItems(int rx) {
        this.rxItems = rx;
        if(rxItems>=maxItems)ready=true;
    }

    public boolean getRxStatus(int which) {
        return rxStatus[which];
    }

    public void setRxStatus(int which,boolean status) {
        rxStatus[which] = status;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}

package com.blautic.pikkucam.cfg;

public class CfgOrder {

    public int number;

    public int type;
    public String uuid;
    public byte[] order;

    public CfgOrder(int type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public byte[] getOrder() {
        return order;
    }

    public void setOrder(byte[] order) {
        this.order = order;
    }
}

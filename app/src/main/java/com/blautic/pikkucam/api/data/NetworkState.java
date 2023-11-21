package com.blautic.pikkucam.api.data;

public class NetworkState {
    public static final NetworkState SUCCESS = new NetworkState(Status.SUCCESS);
    public static final NetworkState RUNNING = new NetworkState(Status.RUNNING);
    public Status status;
    public String msg;

    public NetworkState(Status status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public NetworkState(Status status) {
        this.status = status;
    }

    public static NetworkState error(String msg) {
        return new NetworkState(Status.FAILED, msg);
    }
}

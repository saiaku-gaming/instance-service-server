package com.valhallagame.instanceserviceserver.model;

public enum InstanceState {
    STARTING, // instance has been created but not registered itself yet.
    READY, // Instance is ready to recieve players.
    FINISHING; // No new players should be routed here. But if someone gets in,
    // it should not be removed.

    public String getValue() {
        return this.name();
    }
}

package com.blautic.pikkucam.adapters;

import com.blautic.pikkucam.api.response.Movement;

public class ModelCustomPair {
    public Boolean enable;
    public Movement movement;

    public ModelCustomPair(Boolean enable, Movement movement) {
        this.enable = enable;
        this.movement = movement;
    }
}
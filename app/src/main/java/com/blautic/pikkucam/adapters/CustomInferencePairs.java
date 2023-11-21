package com.blautic.pikkucam.adapters;

import androidx.room.ColumnInfo;

public class CustomInferencePairs {
    @ColumnInfo(name = "LABEL")
    public String label;

    @ColumnInfo(name = "COUNT(IS_CORRECT)")
    public Integer count;
}

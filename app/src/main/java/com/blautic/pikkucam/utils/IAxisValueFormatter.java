package com.blautic.pikkucam.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

abstract class IAxisValueFormatter extends ValueFormatter {

    public String getFormattedValue(float value, AxisBase axis) {
        return null;
    }

}

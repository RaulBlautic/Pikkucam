package com.blautic.pikkucam.utils;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import kotlin.jvm.internal.Intrinsics;

public class Utils {

    public static final String getTimeLocal(@NotNull String utcTime) {
        Intrinsics.checkNotNullParameter(utcTime, "utcTime");

        String var2;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(utcTime);
            df = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ROOT);
            df.setTimeZone(TimeZone.getDefault());
            var2 = df.format(date);
        } catch (ParseException var4) {
            var4.printStackTrace();
            var2 = "";
        }

        Intrinsics.checkNotNullExpressionValue(var2, "formattedDate");
        return var2;
    }
}

package com.aboutfuture.wundercars.model;

import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

public class CoordinatesTypeConverter {
    @TypeConverter
    public static double[] stringToArray(String data) {
        if (TextUtils.isEmpty(data))
            return new double[]{};

        String[] coordinatesAsStringArray = TextUtils.split(data, ",");
        double[] coordinates = new double[coordinatesAsStringArray.length];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = Double.parseDouble(coordinatesAsStringArray[i]);
        }

        return coordinates;
    }

    @TypeConverter
    public static String arrayToString(double[] coordinates) {
        if (coordinates == null)
            return "";

        String[] coordinatesAsStringArray = new String[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            coordinatesAsStringArray[i] = String.valueOf(coordinates[i]);
        }

        return TextUtils.join(",", coordinatesAsStringArray);
    }
}

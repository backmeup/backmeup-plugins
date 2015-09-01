package org.backmeup.facebook.utils;

public class Mathematics {
    public static double roundDouble(double number, int digitAfterKomma) {
        return Math.round(number * Math.pow(10, digitAfterKomma)) / Math.pow(10, digitAfterKomma);
    }
}

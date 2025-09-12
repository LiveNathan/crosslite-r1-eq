package dev.nathanlively.crosslite_r1_eq;

import java.text.DecimalFormat;

public class LogFormat {

    public static String f(int precision, double value) {
        String pattern = "0." + "0".repeat(precision);
        return new DecimalFormat(pattern).format(value);
    }

    public static String f(int precision, float value) {
        String pattern = "0." + "0".repeat(precision);
        return new DecimalFormat(pattern).format(value);
    }

    public static String f0(double value) {
        return f(0, value);
    }

    public static String f1(double value) {
        return f(1, value);
    }

    public static String f3(double value) {
        return f(3, value);
    }

    public static String f5(double value) {
        return f(5, value);
    }

    public static String f7(double value) {
        return f(7, value);
    }

    public static String f11(double value) {
        return f(11, value);
    }

    public static String f15(double value) {
        return f(15, value);
    }

    public static String f3(float value) {
        return f(3, value);
    }
}

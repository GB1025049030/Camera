package com.freeme.camera.mode.photo.effect.utils;

public class MathUtils {

    public static boolean floatEqual(float a, float b){
        if (Math.abs(a - b)< 0.01){
            return true;
        }
        return false;
    }
}
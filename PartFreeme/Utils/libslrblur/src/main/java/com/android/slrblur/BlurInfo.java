package com.android.slrblur;

public class BlurInfo {
    public int x;
    public int y;
    public int inRadius;
    public int outRadius;

    public BlurInfo() {
        this.x = 0;
        this.y = 0;
        this.inRadius = 0;
        this.outRadius = 0;
    }

    public BlurInfo(BlurInfo info) {
        this();
        if (info != null) {
            update(info.x, info.y, info.inRadius, info.outRadius);
        }
    }

    public BlurInfo update(int x, int y, int inRadius, int outRadius) {
        this.x = x;
        this.y = y;
        this.inRadius = inRadius;
        this.outRadius = outRadius;
        return this;
    }

    @Override
    public String toString() {
        return "BlurInfo{" +
                "x=" + x +
                ", y=" + y +
                ", inRadius=" + inRadius +
                ", outRadius=" + outRadius +
                '}';
    }

    public void copyTo(BlurInfo blurInfo) {
        if (blurInfo != null) {
            blurInfo.update(this.x, this.y, this.inRadius, this.outRadius);
        }
    }


    @Override
    public Object clone() {
        Object object = null;
        try {
            object = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return object;
    }
}

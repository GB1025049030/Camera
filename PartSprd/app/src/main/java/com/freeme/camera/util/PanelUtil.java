package com.freeme.camera.util;

import android.content.Context;
import android.content.res.TypedArray;

import com.android.camera2.R;

import java.util.ArrayList;

public class PanelUtil {

    public static class IconAndDes {
        public int buttonType;
        public int id;
        public int desID;
        public int iconID;

        public IconAndDes(TypedArray type){
            buttonType = type.getResourceId(0, -1);
            id = type.getResourceId(1,-1);
            if(buttonType == R.integer.button_type_rotateimagebutton){
                iconID = type.getResourceId(2,-1);
            } else if( buttonType == R.integer.button_type_multitoggleimagebutton ){
                desID = type.getResourceId(2,-1);
                iconID = type.getResourceId(3,-1);
            }
        }

        @Override
        public String toString() {
            return "IconAndDes{" +
                    "buttonType=" + buttonType +
                    ", id=" + id +
                    ", desID=" + desID +
                    ", iconID=" + iconID +
                    '}';
        }
    }

    public static ArrayList<IconAndDes> generatePanelList(
            Context context, int resourceID){
        ArrayList<IconAndDes> list = new ArrayList<IconAndDes>();
        TypedArray types = context.getResources().obtainTypedArray(resourceID);
        if(types != null){
            for (int i = 0; i < types.length(); i++) {
                TypedArray type = context.getResources().obtainTypedArray(
                        types.getResourceId(i, -1));
                IconAndDes temp = new IconAndDes(type);
                list.add(temp);
                type.recycle();
            }
            types.recycle();
        }
        return list;
    }
}
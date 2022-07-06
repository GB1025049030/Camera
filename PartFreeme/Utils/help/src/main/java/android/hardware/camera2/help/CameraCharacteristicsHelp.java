package android.hardware.camera2.help;

import android.hardware.camera2.CameraCharacteristics;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class CameraCharacteristicsHelp {

    private static final Set<String> mCameraCharacteristicsFields;
    private static final Field[] mFields = CameraCharacteristics.class.getFields();

    static {
        mCameraCharacteristicsFields = new HashSet<String>() {
            {
                for (Field field : mFields) {
                    add(field.getName());
                }
            }
        };
    }

    public static boolean hasField(String fieldName) {
        return mCameraCharacteristicsFields.contains(fieldName);
    }

    public static Field getField(String fieldName) {
        for (Field field : mFields){
            if (field.getName().equals(fieldName)){
                return field;
            }
        }
        return null;
    }
}

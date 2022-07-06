package android.hardware.camera2.help;

import android.hardware.camera2.CaptureResult;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class CaptureResultHelp {
    private static final Set<String> mCaptureResultFields;
    private static final Field[] mFields = CaptureResult.class.getFields();

    static {
        mCaptureResultFields = new HashSet<String>() {
            {
                for (Field field : mFields) {
                    add(field.getName());
                }
            }
        };
    }

    public static boolean hasField(String fieldName) {
        return mCaptureResultFields.contains(fieldName);
    }
}

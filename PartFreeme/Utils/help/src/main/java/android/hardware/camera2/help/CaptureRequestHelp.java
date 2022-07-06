package android.hardware.camera2.help;

import android.hardware.camera2.CaptureRequest;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class CaptureRequestHelp {
    private static final Set<String> mCaptureRequestFields;
    private static final Field[] mFields = CaptureRequest.class.getFields();

    static {
        mCaptureRequestFields = new HashSet<String>() {
            {
                for (Field field : mFields) {
                    add(field.getName());
                }
            }
        };
    }

    public static boolean hasField(String fieldName) {
        return mCaptureRequestFields.contains(fieldName);
    }
}

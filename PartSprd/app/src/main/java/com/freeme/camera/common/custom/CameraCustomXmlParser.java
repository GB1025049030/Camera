
package com.freeme.camera.common.custom;

import android.util.Log;
import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CameraCustomXmlParser {
    public static final String TAG = "CameraCustomXmlParser";

    private static final Map<String, Boolean> sParamBooleanValues = new HashMap<>(41); // 41 = (int)(30 / 0.75 + 1);
    private static final Map<String, String> sParamStringValues = new HashMap<>(41);
    private static final Map<String, String> sParamPictureSizeValues = new HashMap<>(22);

    static {
        CameraCustomXmlParser.parsexml();
    }

    public static boolean isSupport(String key) {
        return isSupport(key, true);
    }

    public static boolean isSupport(String key, boolean def) {
        return sParamBooleanValues.containsKey(key) ? sParamBooleanValues.get(key) : def;
    }

    public static String getString(String key) {
        return sParamStringValues.containsKey(key) ? sParamStringValues.get(key) : null;
    }

    public static String getPictureSize(String key) {
        return sParamPictureSizeValues.containsKey(key) ? sParamPictureSizeValues.get(key) : null;
    }

    private static void parsexml() {
        String name, value;
        FileReader reader;
        File file = new File("/system/etc/freemeCamConfig.xml");
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Couldn't find or open xml file: " + file.getName());
            file = new File("/system/vendor/etc/freemeCamConfig.xml");
            try {
                reader = new FileReader(file);
            } catch (FileNotFoundException e1) {
                Log.i(TAG, "Couldn't find or open xml file: " + file.getName());
                return;
            }
        }

        final String backParamPictureSize = getParamPictureSizeName(CameraCustomInterpol.getPictureSize(true));
        final String frontParamPictureSize = getParamPictureSizeName(CameraCustomInterpol.getPictureSize(false));

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(reader);
            for (int type = parser.getEventType(); type != XmlPullParser.END_DOCUMENT; type = parser
                    .next()) {
                switch (type) {
                    case XmlPullParser.START_DOCUMENT:
                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.START_TAG: {
                        if (parser.getAttributeCount() == 0) {
                            break;
                        }
                        name = parser.getAttributeValue(0);
                        value = parser.getAttributeValue(1);
                        if (parser.getName().equals("ParamBoolean")) {
                            sParamBooleanValues.put(name, Boolean.valueOf(value));
                        } else if (parser.getName().equals("Param")) {
                            sParamStringValues.put(name, value);
                        } else if(parser.getName().equals(backParamPictureSize) || parser.getName().equals(frontParamPictureSize)) {
                            if (backParamPictureSize.equals(frontParamPictureSize)
                                    || parser.getName().equals(backParamPictureSize) && Integer.parseInt(name) < 8
                                    || parser.getName().equals(frontParamPictureSize) && Integer.parseInt(name) >= 8) {
                                sParamPictureSizeValues.put(name, value);
                            }
                        }
                        break;
                    }
                }
            }
            reader.close();
        } catch (IOException | XmlPullParserException e) {
            Log.i(TAG, "Got exception parsing permissions", e);
        }
    }

    private static String getParamPictureSizeName(int size) {
        StringBuilder name = new StringBuilder("ParamPictureSize");
        if (size > 0) {
            name.append("_");
            name.append(size);
            name.append("M");
        }
        return name.toString();
    }
}

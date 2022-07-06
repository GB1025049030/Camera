package com.dream.camera.modules.qr;

import android.util.Log;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.regex.Pattern;

public enum ZBarController {
    I;

    private ImageScanner scanner;

    public String scan(byte[] data, int width, int height) {
        if (data == null || width <= 0 || height <= 0) {
            return null;
        }

        if (scanner == null) {
            scanner = new ImageScanner();
            scanner.setConfig(0, Config.X_DENSITY, 3);
            scanner.setConfig(0, Config.Y_DENSITY, 3);
        }

        Image image = new Image(width, height, "Y800");
        image.setData(data);

        int result = scanner.scanImage(image);

        if (result != 0) {
            SymbolSet symbols = scanner.getResults();
            String finalResult;
            for (Symbol sym : symbols) {
                finalResult = sym.getData();
                return isAllNumber(finalResult) ? null : finalResult;
            }
        }
        return null;
    }

    public static boolean isAllNumber(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();

    }
}

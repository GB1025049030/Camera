package com.freeme.camera.modules.slrphoto;

import java.io.File;

public class FileUtils {
    static boolean fileIsExist(String fileName) {
        //传入指定的路径，然后判断路径是否存在
        File file = new File(fileName);
        if (file.exists())
            return true;
        else {
            //file.mkdirs() 创建文件夹的意思
            return file.mkdirs();
        }
    }
}

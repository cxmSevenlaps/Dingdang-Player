package com.example.sevenlaps.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 7laps on 2017/5/9.
 */

public class FileUtils {
    /**
     * @param dir    指定目录
     * @param suffix 制定后缀
     * @return 文件列表
     */
    public static List<File> getFilesByPathAndSuffix(String dir, String suffix) {
        List<File> fileList = new ArrayList<>();
        File file = new File(dir);
        File[] filesArray = file.listFiles();//返回该路径下的所有文件的数组
        for (File fileItem: filesArray){
            if (!fileItem.isDirectory()){
                if(fileItem.getAbsolutePath().endsWith(suffix)){
                    fileList.add(fileItem);
                }
            }else {

                fileList.addAll(getFilesByPathAndSuffix(fileItem.getAbsolutePath(), suffix));
            }
        }
        return fileList;
    }
}

package com.util;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.util.*;

public class MyFileUtils {
    public static final File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    public static List<String> readFile(String fileName) {
        try {
            File file = new File(projectDir,"target/"+fileName);
            List<String> result = new ArrayList<>();
            LineIterator lineIterator = null;
            lineIterator = FileUtils.lineIterator(file);
            while (lineIterator.hasNext()) {
                result.add(lineIterator.next());
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static String mapToJsonString(Map map) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        Iterator<String> iter = map.keySet().iterator();
        while(iter.hasNext()) {
            String key = iter.next();
            String value = map.get(key).toString();
            stringBuilder.append("\""+key+"\""+":"+"\""+value+"\"");
            if (iter.hasNext()){
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static void writeLinesToFile(List<String> lines, String file_name,Boolean append) {
        try {
            File file = new File(projectDir, "target/" + file_name);
            FileUtils.writeLines(file, lines,append);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeFile(String file_name){
        File file = new File(projectDir, "target/" + file_name);
        FileUtils.deleteQuietly(file);
    }


    public static void main(String[] args) {
        List<String> lines = Arrays.asList("AAA", "BBB", "CCC");
        writeLinesToFile(lines,"test.txt",true);
        writeLinesToFile(lines,"test.txt",true);
        writeLinesToFile(lines,"test.txt",true);


    }
}

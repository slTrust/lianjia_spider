package com.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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


    public static Map<String,Object> mergeMap(Map<String,Object> m1, Map<String,Object> m2){
        Map result = new HashMap();
        for (String key: m1.keySet()) {
            System.out.println(key+"-"+m1.get(key));
            result.put(key,m1.get(key));
        }
        for (String key: m2.keySet()) {
            result.put(key,m2.get(key));
            System.out.println(key+"-"+m2.get(key));
        }
        return result;
    }

    public static String mapToJsonString(Map map) {
       return JSON.toJSONString(map);
    }

    public static Map<String,Object> JsonToMap(String  string) {
        Map a = JSONObject.parseObject(string);
        return a;
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

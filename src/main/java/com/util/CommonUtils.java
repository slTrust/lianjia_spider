package com.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    public static Long formatDate(String strDate){
        if((strDate == null) ||"-".equals(strDate) || "暂无数据".equals(strDate)){
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(strDate);
            return date.getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String filterString(String str){
        String regex="([a-zA-Z0-9\u4e00-\u9fa5]+)";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        return matcher.find()?matcher.group(0):str;
    }
}

package com.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}

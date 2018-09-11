package com.goluk.a6.common.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;


/**
 * Created by goluk_lium on 2018/3/23.
 */

public class GsonUtils {

    private static Gson gson = null;
    static {
        if (gson==null){
            gson = new Gson();
        }
    }

    public static String toJsonString(Object o){
         return gson.toJson(o);
    }

    public static <T> T toBean(String jsonString,Class<T> clazz){
        return gson.fromJson(jsonString,clazz);
    }

    public static <T>List<T> toList(String jsonString,Class<T> clazz){
        return gson.fromJson(jsonString,new TypeToken<List<T>>(){}.getType());
    }
}

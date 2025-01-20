package com.zhd.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public class JsonUtil {
    public static String objectToJsonString(Object o){
        return  JSONObject.toJSONString(o);
    }

    public static Object jsonStringToObject(String jstring){
        return JSONObject.parseObject(jstring);
    }


    public static  List jsonStringToList(String jString,Class c){
        return JSON.parseArray(jString,c);
    }

}

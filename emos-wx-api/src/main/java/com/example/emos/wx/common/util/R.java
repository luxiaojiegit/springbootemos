package com.example.emos.wx.common.util;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class R  extends HashMap<String,Object> {
    //成功
    public R(){
        put("code", HttpStatus.SC_OK);
        put("msg","success");
    }
    public static R ok(String msg){
        R r = new R();
        r.put("msg",msg);
        return  r;
    }
    public static R ok(Map<String,Object> map){
        R r = new R();
        r.putAll(map);
        return  r;
    }
    public static R ok(){
        return new R();
    }
    //连续put
    public R put(String key,Object value){
        super.put(key,value);
        return this;
    }
    //失败
    public static R error(){
        return error("未知异常，请联系管理员", HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
    public static R error(String msg){
        return error(msg, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
    public static R error(String msg,int code){
        R r = new R();
        r.put("code", code);
        r.put("msg",msg);
        return r;
    }

}

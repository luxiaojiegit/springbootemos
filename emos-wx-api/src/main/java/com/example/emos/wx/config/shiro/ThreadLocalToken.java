package com.example.emos.wx.config.shiro;


import org.springframework.stereotype.Component;

@Component
public class ThreadLocalToken {
    private ThreadLocal threadLocal = new ThreadLocal();

    public void setToken(String token){
        threadLocal.set(token);
    }

    public String getToken(){
        return (String) threadLocal.get();
    }

    public void clear(){
        threadLocal.remove();
    }
}

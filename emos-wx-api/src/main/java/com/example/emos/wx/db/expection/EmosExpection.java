package com.example.emos.wx.db.expection;

import lombok.Data;

@Data
public class EmosExpection extends RuntimeException {
    private int Code = 500;
    private String msg;

    public EmosExpection(String msg) {
        super(msg);
        this.msg = msg;
    }


    public EmosExpection(String message,Throwable throwable){
        super(message,throwable);
        this.msg = msg;
    }

    public EmosExpection(String message,int code){
        super(message);
        this.msg = message;
        this.Code = code;
    }

    public EmosExpection(String message,Throwable throwable,int code){
        super(message,throwable);
        this.msg = msg;
        this.Code = code;
    }

}

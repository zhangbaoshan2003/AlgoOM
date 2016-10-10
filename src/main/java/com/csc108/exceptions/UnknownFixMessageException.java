package com.csc108.exceptions;

/**
 * Created by zhangbaoshan on 2016/5/7.
 */
public class UnknownFixMessageException extends RuntimeException {
    public UnknownFixMessageException(String msg){
        super(msg);
    }
}
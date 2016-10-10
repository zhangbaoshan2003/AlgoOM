package com.csc108.exceptions;

/**
 * Created by zhangbaoshan on 2016/5/12.
 */
public class OverAllocateException extends RuntimeException {
    public OverAllocateException(String msg){
        super(msg);
    }
}

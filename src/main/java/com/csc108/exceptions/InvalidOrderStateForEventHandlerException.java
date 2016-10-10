package com.csc108.exceptions;

/**
 * Created by zhangbaoshan on 2016/5/13.
 */
public class InvalidOrderStateForEventHandlerException extends RuntimeException {
    public InvalidOrderStateForEventHandlerException(String msg){
        super(msg);
    }
}

package com.csc108.tradingRule.core;

import com.csc108.model.fixModel.order.OrderHandler;

import java.util.LinkedHashMap;

/**
 * Created by LEGEN on 2016/10/29.
 */
public interface IHandler {
    /// <summary>
    /// The name to identity the handler.
    /// </summary>
    String getHandlerName();


    /// <summary>
    /// handle a certain request, then put the result in the request's result parameter
    /// </summary>
    /// <param name="request"></param>
    /// <param name="parameters"></param>
    void handle(OrderHandler orderHandler,LinkedHashMap<String,String> parameters);
}

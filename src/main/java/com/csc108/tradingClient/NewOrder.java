package com.csc108.tradingClient;

import quickfix.fix42.NewOrderSingle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangbaoshan on 2016/10/25.
 */
public class NewOrder {
    public static NewOrderSingle parse(String cmdLine){
        if(cmdLine==null || cmdLine.isEmpty()|| cmdLine==null)
            throw new IllegalArgumentException("No parameter is provided!");

        String[] lines = cmdLine.split(" ");

        NewOrderSingle newOrderSingle = new NewOrderSingle();
        for (int i=0;i<lines.length;i++){
            String[] paras = lines[i].split("=");
            if(paras.length!=2){
                throw new IllegalArgumentException("Can't parse "+paras);
            }

            newOrderSingle.setString(Integer.parseInt(paras[0]),paras[1]);
        }

        return newOrderSingle;
    }
}

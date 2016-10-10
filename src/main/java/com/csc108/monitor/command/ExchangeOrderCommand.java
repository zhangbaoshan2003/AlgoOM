package com.csc108.monitor.command;

import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.ExchangeOrder;
import com.csc108.model.fix.order.OrderPool;
import com.csc108.utility.FixMsgHelper;
import org.apache.commons.cli.CommandLine;
import quickfix.field.ClOrdID;
import quickfix.field.OrigClOrdID;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix42.OrderCancelRequest;

import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/5/27.
 */
public class ExchangeOrderCommand extends CommandBase {
    public ExchangeOrderCommand(){
        options.addOption("o",true,"exchange order id");
    }

    @Override
    public String getFirstLevelKey(){
        return "exchange";
    }

    @Override
    public String getSecondLevelKey(){
        return "order";
    }

    public String cancel(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("o")==false){
                return "exchange order id is not provided!";
            }

            if(cml.getOptionValue("o")==null){
                return "exchange order id is not provided!";
            }

            String exchangeOrderId= cml.getOptionValue("o");

            ExchangeOrder exchangeOrder = OrderPool.getExchangeOrderMap().get(exchangeOrderId);
            if(exchangeOrder==null)
                return "Can't find exchange order:"+exchangeOrderId;

            FixMsgHelper.cancelExchangeOrder(exchangeOrder);

            return "Cancel request has been sent successfully!";

        }catch (Exception ex){
            return "cancel parse error:"+ex;
        }
    }

    public String display(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("o")==false){
                return "exchange order id is not provided!";
            }

            if(cml.getOptionValue("o")==null){
                return "exchange order id is not provided!";
            }

            String exchangeOrderId= cml.getOptionValue("o");

            ExchangeOrder exchangeOrder= OrderPool.getExchangeOrderMap().get(exchangeOrderId);
            if(exchangeOrder==null)
                return "Can't find exchange order:"+exchangeOrderId;

            return exchangeOrder.toString();

        }catch (Exception ex){
            return "cancel parse error:"+ex;
        }
    }
}

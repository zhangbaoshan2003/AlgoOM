package com.csc108.monitor.command;

import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.disruptor.event.EventType;
import com.csc108.log.LogFactory;
import com.csc108.model.PauseResumeEvaluationData;
import com.csc108.model.criteria.TradeAction;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderPool;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import com.csc108.utility.FormattedTable;
import org.apache.commons.cli.CommandLine;
import quickfix.field.*;
import quickfix.fix42.OrderCancelRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/5/25.
 */
public class ClientOrderCommand extends CommandBase {

    public ClientOrderCommand(){
        options.addOption("o",true,"client order id");
        options.addOption("f",true,"force to apply");
    }

    @Override
    public String getFirstLevelKey(){
        return "client";
    }

    @Override
    public String getSecondLevelKey(){
        return "order";
    }

    public String cancel(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("o")==false){
                return "client order id is not provided!";
            }

            if(cml.getOptionValue("o")==null){
                return "client order id is not provided!";
            }

            String clientOrderId= cml.getOptionValue("o");

            ClientOrder clientOrder= OrderPool.getClientOrderMap().get(clientOrderId);
            if(clientOrder==null)
                return "Can't find client order:"+clientOrderId;

            OrderCancelRequest cancelRequest =new OrderCancelRequest(new OrigClOrdID(clientOrderId),
                    new ClOrdID(UUID.randomUUID().toString()),new Symbol(clientOrder.getSymbol()),
                    clientOrder.getOrderSide(),new TransactTime());

            cancelRequest.setString(1,clientOrder.getAccountId());
            cancelRequest.setString(526,clientOrder.getSecondaryCloId());
            cancelRequest.setString(167,clientOrder.getSecurityType());
            cancelRequest.setString(207,clientOrder.getExchangeDest());
            cancelRequest.setString(44,"1");

            FixMsgHelper.handleCancelOrderRequest(cancelRequest, clientOrder.getSessionID());

            return "Cancel request has been sent successfully!";

        }catch (Exception ex){
            LogFactory.error("cancel parse error:",ex);
            return "cancel parse error:"+ex;
        }
    }

    public String apply_cancel(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            String fValue="false";
            boolean forceToApply=false;
            if(cml.hasOption("f")){
                fValue = cml.getOptionValue("f");
                if(Boolean.parseBoolean(fValue)==true)
                        forceToApply=true;

            }else{
                forceToApply=false;
            }

            //return fValue+"="+Boolean.toString(forceToApply) ;

            if(cml.hasOption("o")==false){
                return "client order id is not provided!";
            }

            if(cml.getOptionValue("o")==null){
                return "client order id is not provided!";
            }

            String clientOrderId= cml.getOptionValue("o");

            ClientOrder clientOrder= OrderPool.getClientOrderMap().get(clientOrderId);
            if(clientOrder==null)
                return "Can't find client order:"+clientOrderId;

            if(FixUtil.IsOrderCompleted(clientOrder.getOrdStatus())==true){
                return String.format("Order has been %s, can be canceled now!",clientOrder.getFixStatusDisplay());
            }

            DisruptorController controller = clientOrder.getOrderHandler().getController();

            if(forceToApply){
                controller.enqueueEvent(EventType.APPLY_CANCEL_MANUALLY, clientOrder.getOrderHandler(), null);
                Thread.sleep(200);
                return String.format("Cancel the order directly, currently the order's status is %s",clientOrder.getFixStatusDisplay()) ;
            }else{

                OrderCancelRequest cancelRequest =new OrderCancelRequest(new OrigClOrdID(clientOrderId),
                        new ClOrdID(UUID.randomUUID().toString()),new Symbol(clientOrder.getSymbol()),
                        clientOrder.getOrderSide(),new TransactTime());

                cancelRequest.setString(1,clientOrder.getAccountId());
                cancelRequest.setString(526,clientOrder.getSecondaryCloId());
                cancelRequest.setString(167,clientOrder.getSecurityType());
                cancelRequest.setString(207,clientOrder.getExchangeDest());
                cancelRequest.setString(44,"1");

                FixMsgHelper.handleCancelOrderRequest(cancelRequest, clientOrder.getSessionID());
                Thread.sleep(200);
                return String.format("Cancel request has been sent. Currently the order's status is %s",clientOrder.getFixStatusDisplay()) ;
            }

        }catch (Exception ex){
            LogFactory.error("cancel parse error:",ex);
            return "cancel parse error:"+ex;
        }
    }

    public String display(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("o")==false){
                return "client order id is not provided!";
            }

            if(cml.getOptionValue("o")==null){
                return "client order id is not provided!";
            }

            String clientOrderId= cml.getOptionValue("o");

            ClientOrder clientOrder= OrderPool.getClientOrderMap().get(clientOrderId);
            if(clientOrder==null)
                return "Can't find client order:"+clientOrderId;

            return clientOrder.toString();

        }catch (Exception ex){
            LogFactory.error("cancel parse error:",ex);
            return "cancel parse error:"+ex;
        }
    }

    public String condition(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("o")==false){
                return "client order id is not provided!";
            }

            if(cml.getOptionValue("o")==null){
                return "client order id is not provided!";
            }

            String clientOrderId= cml.getOptionValue("o");

            ClientOrder clientOrder= OrderPool.getClientOrderMap().get(clientOrderId);
            if(clientOrder==null){
                return "Can't find client order:"+clientOrderId;
            }

            if(clientOrder.getOrderHandler().isConditionalOrder()==false){
               return "None conditional order!";
            }

            return clientOrder.getOrderHandler().getCondition().toString();

        }catch (Exception ex){
            LogFactory.error("cancel parse error:",ex);
            return "get condition error:"+ex;
        }
    }

    public String pause(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("o")==false){
                return "client order id is not provided!";
            }

            if(cml.getOptionValue("o")==null){
                return "client order id is not provided!";
            }

            String clientOrderId= cml.getOptionValue("o");

            boolean foceToApply=true;
//            if(cml.hasOption("f")){
//                foceToApply=true;
//            }

            ClientOrder clientOrder= OrderPool.getClientOrderMap().get(clientOrderId);
            if(clientOrder==null)
                return "Can't find client order:"+clientOrderId;

            DisruptorController controller = clientOrder.getOrderHandler().getController();
            PauseResumeEvaluationData evaluationData= new PauseResumeEvaluationData(TradeAction.Pause,foceToApply);
            controller.enqueueEvent(EventType.PAUSE_RESUME,clientOrder.getOrderHandler(),evaluationData);

        }catch (Exception ex){
            LogFactory.error("cancel parse error:",ex);
            return "pause order error:"+ex;
        }

        return "Pause request sent successfully";
    }

    public String resume(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("o")==false){
                return "client order id is not provided!";
            }

            if(cml.getOptionValue("o")==null){
                return "client order id is not provided!";
            }

            String clientOrderId= cml.getOptionValue("o");


            boolean foceToApply=true;
//            if(cml.hasOption("f")){
//                foceToApply=true;
//            }

            ClientOrder clientOrder = OrderPool.getClientOrderMap().get(clientOrderId);
            if(clientOrder==null)
                return "Can't find client order:"+clientOrderId;

            DisruptorController controller = clientOrder.getOrderHandler().getController();
            PauseResumeEvaluationData evaluationData= new PauseResumeEvaluationData(TradeAction.Pause,foceToApply);
            controller.enqueueEvent(EventType.PAUSE_RESUME,clientOrder.getOrderHandler(),evaluationData);

        }catch (Exception ex){
            LogFactory.error("resume parse error:",ex);
            return "resume order error:"+ex;
        }
        return "done!";
    }

    public String list_all_orders (String[] args){
        try{
            FormattedTable table = new FormattedTable();
            List<Object> row = new ArrayList<Object>();
            row.add("clOrdId");
            row.add("exgOrdOd");
            //row.add("secondaryClOrdId");
            row.add("side");
            row.add("price");
            row.add("orderQty");
            row.add("cumQty");
            row.add("leavesQty");
            row.add("avgPx");
            //row.add("type");
            row.add("symbol");

            row.add("exDestination");
            //row.add("securityType");
            //row.add("marginType");

            row.add("startTime");
            row.add("endTime");

            row.add("orderStatus");
            row.add("orderState");
            row.add("accountId");
            table.AddRow(row);

            String exchangeOrderId="";
            for (ClientOrder clientOrder:OrderPool.getClientOrderMap().values() ){
                row = new ArrayList<Object>();
                row.add(clientOrder.getClientOrderId());

                if(clientOrder.getOrderHandler().getExchangeOrders().size()>0){
                    exchangeOrderId = clientOrder.getOrderHandler().getExchangeOrders().get(0).getClientOrderId();
                }
                row.add(exchangeOrderId);
                //row.add("");
                row.add(clientOrder.getOrderSide());

                row.add(Double.toString(clientOrder.getLastPrice()));
                row.add(Double.toString(clientOrder.getOrderQty()));
                row.add(Double.toString(clientOrder.getCumQty()));
                row.add(Double.toString(clientOrder.getLeavesQty()));
                row.add(Double.toString(clientOrder.getPrice()));
                //row.add("Limit");
                row.add(clientOrder.getSymbol());

                row.add(clientOrder.getExchangeDest());
                //row.add(clientOrder.getSecurityType());
                //row.add(clientOrder.get);
                if(clientOrder.getEffectiveTime()!=LocalDateTime.MAX){
                    row.add(clientOrder.getEffectiveTime());
                }else{
                    row.add("");
                }

                if(clientOrder.getExpireTime()!=LocalDateTime.MAX){
                    row.add(clientOrder.getExpireTime());
                }else{
                    row.add("");
                }

                row.add(clientOrder.getFixStatusDisplay());
                row.add(clientOrder.getOrderStateDisplay());
                row.add(clientOrder.getAccountId());

                table.AddRow(row);
            }
            return table.toString();
        }catch (Exception ex){
            LogFactory.error("Parse error!",ex);
            return "parse error"+ex.getMessage();
        }
    }

}

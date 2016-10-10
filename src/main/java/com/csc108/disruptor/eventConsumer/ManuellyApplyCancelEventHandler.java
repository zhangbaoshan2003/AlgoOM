package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.PauseResumeEvaluationData;
import com.csc108.model.criteria.TradeAction;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/6/8.
 */
public class ManuellyApplyCancelEventHandler extends EventHandlerBase {
    public static ManuellyApplyCancelEventHandler Instance= new ManuellyApplyCancelEventHandler();

    private ManuellyApplyCancelEventHandler(){
    }

    @Override
    public String getHandlerName(){
        return "ManuellyApplyCancelEventHandler";
    }

    public void handle(OmEvent eventSource){
        OrderHandler orderHandler =  (OrderHandler)eventSource.getDataHandler();
        try{
            if(FixUtil.IsOrderCompleted(orderHandler.getClientOrder().getOrdStatus())){
                LogFactory.debug("Can't apply cancel a completed order!\n" + String.format(Alert.PAUSE_RESUME_ERROR, orderHandler.getClientOrder().getClientOrderId()));
                return;
            }

            ClientOrder clientOrder = orderHandler.getClientOrder();

            orderHandler.getClientOrder().setOrdStatus(new OrdStatus(OrdStatus.CANCELED));

            ExecutionReport canceledReport = new ExecutionReport(
                    new OrderID(clientOrder.getClientOrderId()),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.CANCELED),
                    new OrdStatus(OrdStatus.CANCELED),
                    new Symbol(clientOrder.getSymbol()),
                    clientOrder.getOrderSide(),
                    new LeavesQty(clientOrder.getLeavesQty()),
                    new CumQty(clientOrder.getCumQty()),
                    new AvgPx(clientOrder.getPrice())
            );

            canceledReport.set(new ClOrdID(clientOrder.getClientOrderId()));
            canceledReport.set(new OrigClOrdID(clientOrder.getClientOrderId()));
            canceledReport.set(new LastPx(clientOrder.getPrice()));
            canceledReport.set(new LastShares(clientOrder.getLeavesQty()));
            canceledReport.set(new OrderQty(clientOrder.getOrderQty()));
            canceledReport.set(new Text("Canceled by user command"));

            FixMsgHelper.sendMessage(canceledReport, clientOrder.getSessionID(), FixMsgHelper.CLIENT_IN_REPORT_CANCELED_MANUALLY, clientOrder.getClientOrderId());

        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major,
                    String.format(Alert.PAUSE_RESUME_ERROR, orderHandler.getClientOrder().getClientOrderId()),
                    "manually apply cancel error!", ex);
        }
    }


}

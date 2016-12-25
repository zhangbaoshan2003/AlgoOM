package com.csc108.decision;

import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.Allocation;
import com.csc108.model.OrderState;
import com.csc108.model.cache.ReferenceDataManager;
import com.csc108.model.data.SessionGroup;
import com.csc108.model.data.TradingSession;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.utility.Alert;
import com.csc108.utility.FixUtil;
import quickfix.field.OrdStatus;
import quickfix.field.Side;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Created by NIUXX on 2016/12/24.
 */
public class OrderStatusDecision extends BaseDecision  {
    private String not_started_msg="Order not started till [%s]";
    private String order_completed_msg="Order is completed as [%s] with leaves qty [%d]";
    private String order_in_frozen="Order is in frozen state";
    private String order_frozen_too_many_rejected="Order is frozen due to too many rejected response";
    private String order_completed="Order has been compleled as %s";
    private String order_not_tradible="Order's trasaction time [%s] is not in any tradible session";
    private String order_expire="Order's transaction time is expired with transaction time [%s] expire time [%s]";

    @Override
    public boolean allocateDecision(OrderHandler orderHandler, Ref<Double> qtyToAllocate_, ArrayList<Allocation> allocationDecisions_, ArrayList<OmEvent> events, ArrayList<String> logLines) {
        //decide if post the start time
        if(orderHandler.getTransactionTime().isBefore(orderHandler.getClientOrder().getEffectiveTime())){
            Alert.fireAlert(Alert.Severity.Info,String.format(Alert.INFO_ORDER_PROCESS_KEY,orderHandler.getClientOrder().getClientOrderId()),
                    String.format(not_started_msg,orderHandler.getClientOrder().getClientId(),orderHandler.getClientOrder().getEffectiveTime()),null);
            return false;
        }

        //decide if posture the expire time
        if(orderHandler.getTransactionTime().isAfter(orderHandler.getClientOrder().getExpireTime())){
            if(FixUtil.isInPendingStatus(orderHandler)==false){
                orderHandler.getClientOrder().setOrdStatus(new OrdStatus(OrdStatus.EXPIRED));
                orderHandler.getClientOrder().setOrderState(OrderState.COMPLETED);

                Alert.fireAlert(Alert.Severity.Info,String.format(Alert.INFO_ORDER_PROCESS_KEY,orderHandler.getClientOrder().getClientOrderId()),
                        String.format(order_expire,orderHandler.getTransactionTime(),
                                orderHandler.getClientOrder().getExpireTime()),null);
                return false;
            }
        }

        //order has been compleleted
        if(FixUtil.isInPendingStatus(orderHandler)==false){
            if(orderHandler.getClientOrder().getOrderSide().getValue()== Side.BUY && orderHandler.getClientOrder()
                    .getLeavesQty()<= ReferenceDataManager.getInstance().getLotSize(orderHandler.getClientOrder().getSecurityId())){
                Alert.fireAlert(Alert.Severity.Info,String.format(Alert.INFO_ORDER_PROCESS_KEY,orderHandler.getClientOrder().getClientOrderId()),
                        String.format(order_completed_msg,orderHandler.getClientOrder().getFixStatusDisplay(),orderHandler.getClientOrder().getLeavesQty()),null);
                return false;
            }
        }

        //order has been frozen
        if(orderHandler.getClientOrder().getOrderState()== OrderState.FROZEN){
            Alert.fireAlert(Alert.Severity.Info,String.format(Alert.INFO_ORDER_PROCESS_KEY,orderHandler.getClientOrder().getClientOrderId()),
                    order_in_frozen,null);
            return false;
        }

        //too many rejected response
        if(orderHandler.getCancelRejectedTimes()>ReferenceDataManager.getInstance().getRejectedTimesBeforeFrozen()
                || orderHandler.getNewRejectedTimes()>ReferenceDataManager.getInstance().getRejectedTimesBeforeFrozen()){
            Alert.fireAlert(Alert.Severity.Info,String.format(Alert.INFO_ORDER_PROCESS_KEY,orderHandler.getClientOrder().getClientOrderId()),
                    order_frozen_too_many_rejected,null);
            return false;
        }

        //if in the tradible session
        if(ReferenceDataManager.getInstance().isInTradibleSession(orderHandler)==false){
            Alert.fireAlert(Alert.Severity.Info,String.format(Alert.INFO_ORDER_PROCESS_KEY,orderHandler.getClientOrder().getClientOrderId()),
                    String.format(order_not_tradible,orderHandler.getTransactionTime()),null);
            return false;
        }

        if(FixUtil.IsClientOrderCompleted(orderHandler)){
            Alert.fireAlert(Alert.Severity.Info,String.format(Alert.INFO_ORDER_PROCESS_KEY,orderHandler.getClientOrder().getClientOrderId()),
                    String.format(order_completed,
                            orderHandler.getClientOrder().getFixStatusDisplay()),null);
            return false;
        }

        return true;
    }
}

package com.csc108.decision.pegging;

import com.csc108.decision.BaseDecision;
import com.csc108.decision.configuration.PegConfiguration;
import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.Allocation;
import com.csc108.model.AllocationCategory;
import com.csc108.model.AllocationDecisionType;
import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.model.market.OrderBook;
import com.csc108.utility.Alert;
import com.csc108.utility.FormattedTable;
import com.sun.javaws.exceptions.InvalidArgumentException;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangbaoshan on 2016/8/2.
 */
public class PeggingDecision  extends BaseDecision {
    private boolean peggingAlertFiredBefore=false;

    private String getAlertKey(OrderHandler orderHandler){
        return String.format(Alert.PEG_NO_ALLOCATION_ERROR,orderHandler.getClientOrder().getClientOrderId());
    }

    public void peggingAllocate(ArrayList<Allocation> allocationDecisions,double qtyToAllocate,
                                 int level,double displaySize,Side side,OrderBook ob,
                                 int minimumTradeSize){

        if(displaySize*level<=qtyToAllocate){
            for (int i=0;i<level;i++){
                Allocation allocation = new Allocation();
                if(side.getValue()==Side.BUY){
                    allocation.setAllocatedPrice(ob.getBidPrice(i));
                }else{
                    allocation.setAllocatedPrice(ob.getAskPrice(i));
                }
                allocation.setAllocatedQuantity(displaySize);
                allocation.setCategory(AllocationCategory.Passive);
                allocation.setDecisionType(AllocationDecisionType.Pegging);
                allocationDecisions.add(allocation);
            }
            return;
        }

        if(minimumTradeSize>qtyToAllocate){
            Allocation allocation = new Allocation();
            if(side.getValue()==Side.BUY){
                allocation.setAllocatedPrice(ob.getBidPrice(level));
            }else{
                allocation.setAllocatedPrice(ob.getAskPrice(level));
            }
            allocation.setAllocatedQuantity(displaySize);
            allocation.setCategory(AllocationCategory.Passive);
            allocation.setDecisionType(AllocationDecisionType.Pegging);
            allocationDecisions.add(allocation);
            return;
        }

        int round = (int)qtyToAllocate/(minimumTradeSize*level);
        if(round>0){
            for (int i=0;i<level;i++){
                Allocation allocation = new Allocation();
                if(side.getValue()==Side.BUY){
                    allocation.setAllocatedPrice(ob.getBidPrice(i));
                }else{
                    allocation.setAllocatedPrice(ob.getAskPrice(i));
                }
                allocation.setAllocatedQuantity(minimumTradeSize*round);
                allocation.setCategory(AllocationCategory.Passive);
                allocation.setDecisionType(AllocationDecisionType.Pegging);
                allocationDecisions.add(allocation);
            }

            double leftQty = qtyToAllocate - minimumTradeSize*level*round;
            if(leftQty>0){
                for(Allocation allocation:allocationDecisions){
                    double currentQty = allocation.getAllocatedQuantity();
                    allocation.setAllocatedQuantity(currentQty+minimumTradeSize);
                    leftQty = leftQty -minimumTradeSize;
                    if(leftQty==0.0)
                        break;
                }
            }

            if(leftQty<0)
                throw new IllegalArgumentException("Wrong allocation happened!");
            return;
        }

        for(int i=0;i<level;i++){
            Allocation allocation = new Allocation();
            if(side.getValue()==Side.BUY){
                allocation.setAllocatedPrice(ob.getBidPrice(i));
            }else{
                allocation.setAllocatedPrice(ob.getAskPrice(i));
            }
            allocation.setAllocatedQuantity(minimumTradeSize);
            allocation.setCategory(AllocationCategory.Passive);
            allocation.setDecisionType(AllocationDecisionType.Pegging);
            allocationDecisions.add(allocation);

            qtyToAllocate = qtyToAllocate-minimumTradeSize;

            if(qtyToAllocate==0.0)
                return;

            if(qtyToAllocate<0)
                throw new IllegalArgumentException("Wrong allocation happened!");
        }
    }

    @Override
    public String toString(){
        return "PeggingDecision";
    }

    @Override
    public boolean allocateDecision(OrderHandler orderHandler, Ref<Double> qtyToAllocate_,
                                    ArrayList<Allocation> allocationDecisions_,
                                    ArrayList<OmEvent> events,  ArrayList<String> logLines) {

        PegConfiguration configuration = orderHandler.getPegConfiguration();
        if(configuration==null){
            throw new IllegalArgumentException("There is no pegging configuration for pegging order!");
        }

        String securityID = orderHandler.getClientOrder().getSecurityId();
        logLines.add("Security id : "+securityID);
        OrderBook latestOrderbook = OrderbookDataManager.getInstance().getLatestOrderBook(securityID);
        if(latestOrderbook==null){

            Alert.fireAlert(Alert.Severity.Major,getAlertKey(orderHandler),"OrderBook "
                    +orderHandler.getClientOrder().getSecurityId()+" is not available for the pegging order!",null);

            throw new IllegalArgumentException("No order book available for " + orderHandler.getClientOrder().getSymbol() + ", no pegging");
        }

        Alert.clearAlert(getAlertKey(orderHandler));

        logLines.add("The latest orderbook @"+latestOrderbook.toString());
        logLines.add("The latest order processed time @"+orderHandler.getLastProcessedTime());

        try{
            logLines.add("Begin pegging allocation @ configuration:");
            logLines.add(configuration.toString());

            peggingAllocate(allocationDecisions_, qtyToAllocate_.getValue(),
                    configuration.getLadderLevel(),
                    configuration.getDisplaySize(),
                    orderHandler.getClientOrder().getOrderSide(), latestOrderbook, 100);

            double totalAllocatedQty = allocationDecisions_.stream().mapToDouble(x->x.getAllocatedQuantity())
                    .sum();

            qtyToAllocate_.setValue(qtyToAllocate_.getValue()-totalAllocatedQty);

            if(peggingAlertFiredBefore==true){
                Alert.clearAlert("Order @"+orderHandler.getClientOrder().getClientOrderId()+" pegging allocation error!");
            }

            peggingAlertFiredBefore=false;
            logLines.add("End pegging with the follow allocations:");

            FormattedTable table = new FormattedTable();
            List<Object> row = new ArrayList<Object>();
            row.add("allocation_id");
            row.add("category");
            row.add("decision");
            row.add("allocation_qty");
            row.add("allocation_px");
            table.AddRow(row);

            for (Allocation al:allocationDecisions_){
                row = new ArrayList<Object>();
                row.add(al.getID()) ;
                row.add(al.getCategory());
                row.add(al.getDecisionType());
                row.add(al.getAllocatedQuantity());
                row.add(al.getAllocatedPrice());
                table.AddRow(row);
            }
            logLines.add(table.toString());

        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Critical,
                    "Order @"+orderHandler.getClientOrder().getClientOrderId()+" pegging allocation error!",ex.getMessage(),ex);
            peggingAlertFiredBefore=true;
        }

        return true;
    }
}

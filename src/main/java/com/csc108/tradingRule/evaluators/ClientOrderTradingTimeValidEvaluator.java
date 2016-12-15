package com.csc108.tradingRule.evaluators;

import com.csc108.log.LogFactory;
import com.csc108.model.cache.ReferenceDataManager;
import com.csc108.model.fixModel.order.OrderPool;
import com.csc108.tradingRule.core.BaseEvaluator;
import com.csc108.utility.Alert;
import quickfix.field.OrdStatus;

/**
 * Created by zhangbaoshan on 2016/12/13.
 */
public class ClientOrderTradingTimeValidEvaluator extends BaseEvaluator {
    @Override
    public String getEvaluatorName() {
        return "ClientOrderTradingTimeValidEvaluator";
    }

    @Override
    protected boolean evaluate(){
        boolean expectedValidOrNot = Boolean.parseBoolean(criteria);
        boolean validateResult=false;
        try{
            ReferenceDataManager.getInstance().validateNewOrder(orderHandler);
            validateResult=true;
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major,
                    String.format(Alert.VALIDATE_ORDER_ERROR,orderHandler.getClientOrder().getClientOrderId()),ex.getMessage(),ex);
            validateResult = false;
        }
        return expectedValidOrNot==validateResult;
    }

    @Override
    public void validate(String _criteria){
        try{
            boolean expectedValidOrNot = Boolean.valueOf(_criteria);
        }catch (Exception ex){
            throw new IllegalArgumentException("Invalid criteria set for ClientOrderTradingTimeEvaluator @ "+criteria);
        }
    }
}

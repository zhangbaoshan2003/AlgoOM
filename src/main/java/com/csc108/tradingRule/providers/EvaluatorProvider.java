package com.csc108.tradingRule.providers;

import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.tradingRule.evaluators.*;
import com.csc108.tradingRule.handlers.RejectClientOrderHandler;

import java.util.HashMap;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class EvaluatorProvider {
    private static final HashMap<String,IEvaluator> evaluators = new HashMap<>();
    public static final HashMap<String,IEvaluator> getEvaluators(){
        return evaluators;
    }

    public static void initialize() {
        IEvaluator evaluator = new AccountIDEvaluator();
        evaluators.put(evaluator.getEvaluatorName(),evaluator);

        evaluator = new NumOfOrdersPerAccountEvaluator();
        evaluators.put(evaluator.getEvaluatorName(),evaluator);

        evaluator = new AlwaysTrueEvaluator();
        evaluators.put(evaluator.getEvaluatorName(),evaluator);

        evaluator = new AlgoOrderTypeEvaluator();
        evaluators.put(evaluator.getEvaluatorName(),evaluator);

        evaluator = new ClientOrderTradingTimeValidEvaluator();
        evaluators.put(evaluator.getEvaluatorName(),evaluator);
    }
}

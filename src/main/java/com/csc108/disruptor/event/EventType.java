package com.csc108.disruptor.event;

import com.csc108.disruptor.eventConsumer.*;
import quickfix.fix42.NewOrderSingle;

/**
 * Created by zhangbaoshan on 2016/5/6.
 * For labelling what type of event has been triggered and how to process it accordingly
 */
public enum EventType {
    NEW_SINGLE_ORDER(NewOrderEventHandler.Instance),
    CANCEL_ORDER_REQUEST(OrderCancelRequestEventHandler.Instance),
    EVALUATION(EvaluationEventHandler.Instance),
    CANCEL_REJECTED(CancelRejectedEventHandler.Instance),
    WAKEUP(WakeupEventHandler.Instance),
    PAUSE_RESUME(PauseResumeEventHandler.Instance),
    APPLY_CANCEL_MANUALLY(ManuellyApplyCancelEventHandler.Instance),
    MARKET_DATA_UPDATED(MarketDataUpdatedEventHandler.Instance),
    PERFORMANCE_TEST(PerformanceCounterEventHandler.Instance),
    EXECUTION_REPORT(ExecutionReportEventHandler.Instance);

    private final EventHandlerBase handler;
    EventType(EventHandlerBase handler){
        this.handler=  handler;
    }
    public EventHandlerBase getHandler(){
        return this.handler;
    }
}

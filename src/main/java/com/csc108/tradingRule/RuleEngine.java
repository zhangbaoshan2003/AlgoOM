package com.csc108.tradingRule;

import com.csc108.exceptions.TimeOutException;
import com.csc108.log.LogFactory;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.tradingRule.core.IRule;
import com.csc108.tradingRule.core.TradingRule;
import com.csc108.tradingRule.providers.EvaluatorProvider;
import com.csc108.tradingRule.providers.HandlerProvider;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import com.csc108.utility.AlertManager;
import com.sun.javaws.exceptions.InvalidArgumentException;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class RuleEngine {
    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final long defaultLockTimeOut=5000;

    private static boolean evaluate(OrderHandler orderHandler,List<HashMap<String,String>> evaluatorCriterias) throws Exception {
        for (HashMap<String,String> evalCriteria:evaluatorCriterias){
            for (String key:evalCriteria.keySet()){
                IEvaluator evaluator  = EvaluatorProvider.getEvaluators().get(key);
                boolean result= evaluator.evaluate(orderHandler,evalCriteria.get(key));
                if(result==false)
                    return false;
            }
        }
        return true;
    }

    private static void handle(OrderHandler orderHandler,List<HashMap<String,LinkedHashMap<String,String>>> handlersWithParameters){
        for (HashMap<String,LinkedHashMap<String,String>> handlerWithParam:handlersWithParameters){
            for (String key:handlerWithParam.keySet()){
                IHandler handler  = HandlerProvider.getHandlers().get(key);
                handler.handle(orderHandler,handlerWithParam.get(key));
            }
        }
    }

    public static void process(OrderHandler orderHandler) throws Exception {
        boolean readLockAvailable=false;
        try {
            readLockAvailable = readWriteLock.readLock().tryLock(defaultLockTimeOut, TimeUnit.MILLISECONDS);
            if(!readLockAvailable)
                throw new TimeOutException(String.format("Unable to acquire the lock after %s milliseconds", defaultLockTimeOut));

            List<IRule> rules= TradingRuleProvider.getInstance().getTradingRules();
            for (IRule rule:rules){
                try{
                    if(evaluate(orderHandler,rule.getEvaluatorCriterias())){
                        handle(orderHandler,rule.getHandlerParameters());
                        return;
                    }
                }catch (Exception ex){
                    LogFactory.error(String.format("Process rule %s error!",rule.getRuleName()),ex);
                    throw ex;
                }
            }

            throw new IllegalArgumentException(String.format( "No trading rule found for order %s",orderHandler.getClientOrder().getClientOrderId()));
        }
        finally {
            if(readLockAvailable==true)
                readWriteLock.readLock().unlock();
        }
    }

    public void updateRuleEvaluator(String ruleName,String evaluatorName,String criteria) throws Exception {
        boolean readLockAvailable = readWriteLock.readLock().tryLock(defaultLockTimeOut, TimeUnit.MILLISECONDS);

        if(!readLockAvailable)
            throw new TimeOutException(String.format("Unable to acquire the lock after %s milliseconds", defaultLockTimeOut));

        IRule tradingRule=null;
        Optional<IRule> ruleOptional = TradingRuleProvider.getInstance().getTradingRules()
                .stream().filter(x->x.getRuleName().equals(ruleName)).findFirst();

        if(ruleOptional.isPresent()==false){
            throw new IllegalArgumentException(String.format("Can't find rule %s to adjust",ruleName));
        }

        tradingRule = ruleOptional.get();

        boolean lockAvailable = false;
        try{
            lockAvailable= readWriteLock.writeLock().tryLock(defaultLockTimeOut, TimeUnit.MILLISECONDS);

        }catch (Exception ex){
            throw new TimeoutException("Failed to acquire write lock after @ "+defaultLockTimeOut);
        }finally {
            if(lockAvailable==true)
                readWriteLock.writeLock().unlock();
        }
    }
}
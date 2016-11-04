package com.csc108.tradingRule.providers;

import com.csc108.log.LogFactory;
import com.csc108.model.IEvaluationData;
import com.csc108.model.data.Security;
import com.csc108.model.data.SecurityType;
import com.csc108.model.market.OrderBook;
import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.tradingRule.core.IRule;
import com.csc108.tradingRule.core.TradingRule;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class TradingRuleProvider {

    private final ArrayList<IRule> tradingRules = new ArrayList<>();
    public final ArrayList<IRule> getTradingRules(){
        return tradingRules;
    }

    private static final TradingRuleProvider instance = new TradingRuleProvider();
    public static final TradingRuleProvider getInstance(){
        return instance;
    }

    public void initialize(String ruleFileName) throws Exception {
        EvaluatorProvider.initialize();
        HandlerProvider.initialize();

        String tradingRuleFilePath = String.format("configuration/tradingRules/%s",ruleFileName);
        if(Files.exists(Paths.get(tradingRuleFilePath))==false){
            throw new FileNotFoundException(String.format("Can't find trading rule %s to initialize trading rule provider!",tradingRuleFilePath));
        }

        Document doc = new Document();
        SAXBuilder builder = new SAXBuilder();
        doc = builder.build(tradingRuleFilePath);

        List<Element> elementLis = doc.getRootElement().getChildren("Rule");
        for (Element e:elementLis){
            TradingRule rule = new TradingRule(e.getValue());

            //initialize evaluators and their corresponding criteria
            List<Element> evaluators = e.getChild("Evaluators").getChildren("Evaluator");
            for (Element eval:evaluators){
                String evaluatorName = eval.getAttributeValue("Name");
                String criteria = eval.getAttributeValue("Criteria");

                IEvaluator evaluator = EvaluatorProvider.getEvaluators().get(evaluatorName);
                if(evaluator==null)
                    throw new IllegalArgumentException("Can't find evaluator @ "+evaluatorName);

                HashMap<String,String> evaPara = new HashMap<String,String>();
                evaPara.put(evaluatorName,criteria);
                rule.getEvaluatorCriterias().add(evaPara);
            }

            //initialize handlers and their parameters
            List<Element> handlersElement = e.getChild("Handlers").getChildren("Handler");
            for (Element handlerElement:handlersElement){
                String handlerName = handlerElement.getAttributeValue("Name");
                IHandler handler= HandlerProvider.getHandlers().get(handlerName);
                if(handler==null)
                    throw new IllegalArgumentException("Can't find handler @ "+handlerName);

                LinkedHashMap<String,String> handlerParametersMap= new LinkedHashMap<String,String>();
                if(handlerElement.getChild("Parameters")!=null){
                    List<Element> paraElement= handlerElement.getChild("Parameters").getChildren("Para");
                    if(paraElement!=null){
                        for (Element para:paraElement){
                            String paraName = para.getAttributeValue("Name");
                            String value = para.getAttributeValue("Value");
                            handlerParametersMap.put(paraName,value);
                        }
                    }
                }

                HashMap<String,LinkedHashMap<String,String>> handlersWithParaMap = new HashMap<>();
                handlersWithParaMap.put(handler.getHandlerName(),handlerParametersMap);

                rule.getHandlerParameters().add(handlersWithParaMap);
            }

            tradingRules.add(rule);
        }
    }


}

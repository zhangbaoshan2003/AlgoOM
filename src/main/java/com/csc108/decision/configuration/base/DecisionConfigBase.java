package com.csc108.decision.configuration.base;

import com.csc108.decision.IDecisionConfig;
import com.csc108.log.LogFactory;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.providers.EvaluatorProvider;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by NIUXX on 2016/12/22.
 */
public abstract class DecisionConfigBase implements IDecisionConfig {
    private String configId;
    private List<IEvaluator> evaluators = new ArrayList<>();
    private HashMap<String,String> criterias = new HashMap<>();

    public void readConfigData(Element dataElement) throws Exception {

    }

    @Override
    public void init(Element configElement) throws Exception {
        configId = configElement.getAttributeValue("Id");

        List<Element> evaluatorElements = configElement.getChild("Evaluators").getChildren();
        evaluatorElements.forEach(x->{
            String evaluatorName = x.getAttributeValue("Name");
            String criteria= x.getAttributeValue("Criteria");

            IEvaluator evaluator = EvaluatorProvider.getEvaluators().get(evaluatorName);
            evaluators.add(evaluator);
            criterias.put(evaluatorName,criteria);
        });

        readConfigData(configElement.getChild("Data"));
    }

    @Override
    public String getConfigId(){
        return configId;
    }

    @Override
    public boolean evaluate(OrderHandler orderHandler) {
        for (IEvaluator e : evaluators){
            try{
                if(e.evaluate(orderHandler,criterias.get(e.getEvaluatorName()))==false)
                    return false;
            }catch (Exception ex){
                LogFactory.error("Error happened when evaluating  @ "+criterias.get(e.getEvaluatorName()),ex);
                return false;
            }
        }
        return false;
    }
}

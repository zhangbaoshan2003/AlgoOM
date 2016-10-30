package com.csc108.model.cache;

import com.csc108.decision.IDecisionConfig;
import com.csc108.log.LogFactory;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by zhangbaoshan on 2016/5/11.
 */
public class DecisionConfigCache<T extends IDecisionConfig> extends AlgoCache<String, T> {

    public void put(String configurationId,T t){
        super.put(configurationId,t);
    }

    public T get(String configurationId){
        T result=null;
        result= super.get(configurationId);
        return result;
    }

    private static DecisionConfigCache _instance = new DecisionConfigCache();

    public static DecisionConfigCache getInstance() {
        return _instance;
    }

    public void initialize() throws Exception {
        try {
            Path filePath= Paths.get("TradingRules.xml");

        }catch (InvalidPathException ex){
            LogFactory.error("Can't open TradingRules.xml file to initialize decision configuration",ex);
            throw ex;
        }


    }
}

package com.csc108.model.cache;

import com.csc108.decision.IDecisionConfig;
import com.csc108.log.LogFactory;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Created by zhangbaoshan on 2016/5/11.
 */
public class DecisionConfigCache<T extends IDecisionConfig> extends AlgoCache<String, T> {

    public void put(T t){
        super.put(t.getClass().getName(),t);
    }

    public T get(String className){
        T result=null;
        result= super.get(className);
        return result;
    }
}

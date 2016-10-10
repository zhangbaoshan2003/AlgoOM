package com.csc108.drools;

import org.drools.KnowledgeBase;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangbaoshan on 2016/1/26.
 */
public class KnowledgeBasePool {
    private static ConcurrentHashMap<Integer,KnowledgeBase> base=null;
    public static  ConcurrentHashMap<Integer,KnowledgeBase> getCacheResource() {
        if (base == null)
            base=new ConcurrentHashMap<Integer,KnowledgeBase>();
        return base;
    }
    public static void addCache(Integer key,KnowledgeBase value){
        ConcurrentHashMap<Integer,KnowledgeBase> dictionary=getCacheResource();
        if(!dictionary.containsKey(key)){
            dictionary.put(key,value);
        }
    }
    public static KnowledgeBase getCache(Integer key){
        ConcurrentHashMap<Integer,KnowledgeBase> dictionary=getCacheResource();
        return dictionary.get(key);
    }
}

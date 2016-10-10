package com.csc108.drools;

import com.csc108.log.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangbaoshan on 2016/1/27.
 */
public class DroolsUtility {

    public static void init(String[] droolsFilePath,int type){
        List<Resource> list=null;
        try{
            KnowledgeBuilder kbuilder= KnowledgeBuilderFactory.newKnowledgeBuilder();

            for(String filePath:droolsFilePath){
                String droolFile = "configuration/drools/"+filePath;
                System.out.println(droolFile);
                list=new ArrayList<Resource>();
                Resource resource = ResourceFactory.newFileResource(droolFile);
                kbuilder.add(resource, ResourceType.DRL);
                list.add(resource);
            }

            KnowledgeBuilderErrors errors = kbuilder.getErrors();
            if (errors.size() > 0) {
                for (KnowledgeBuilderError error : errors) {
                    System.err.println(error);
                    LogFactory.droolsLog("init drolls error:" + error);
                }
                throw new Error("Initialize drools failed!",null);
            }
            KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
            KnowledgeBasePool.addCache(type,kbase);
        }catch (Exception ex){
            ex.printStackTrace();
            LogFactory.error("Init drools error!", ex);
        }
    }

    public static KnowledgeBase readKnowledgeBase(int type) throws Exception {
        return KnowledgeBasePool.getCache(type);
    }

    public static void processMessage(Object task, Integer droolsType) throws Exception {
        try {
            KnowledgeBase knowledgeBase  = readKnowledgeBase(droolsType);
            StatelessKnowledgeSession session = knowledgeBase.newStatelessKnowledgeSession();
            session.execute(task);
        }catch (Exception ex){
            LogFactory.error("Handle drools task error!",ex);
            throw ex;
        }
    }
}

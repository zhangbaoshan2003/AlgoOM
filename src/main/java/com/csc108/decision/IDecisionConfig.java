package com.csc108.decision;

import com.csc108.model.fixModel.order.OrderHandler;
import org.jdom2.Element;

import java.util.List;

/**
 * Created by zhangbaoshan on 2016/5/9.
 */
public interface IDecisionConfig  {
     void init(Element configElement) throws Exception ;
     String getConfigId();
     boolean evaluate(OrderHandler orderHandler);
}

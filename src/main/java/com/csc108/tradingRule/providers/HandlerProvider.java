package com.csc108.tradingRule.providers;

import com.csc108.log.LogFactory;
import com.csc108.model.data.Security;
import com.csc108.model.data.SecurityType;
import com.csc108.model.market.OrderBook;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.tradingRule.handlers.AssembleDecisionChainHandler;
import com.csc108.tradingRule.handlers.RejectClientOrderHandler;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class HandlerProvider {
    private static final HashMap<String,IHandler> handlers = new HashMap<>();
    public static final HashMap<String,IHandler> getHandlers(){
        return handlers;
    }

    public static void initialize() throws Exception {
        IHandler handler = new RejectClientOrderHandler();
        handlers.put(handler.getHandlerName(),handler);

        handler = new AssembleDecisionChainHandler();
        handlers.put(handler.getHandlerName(),handler);
    }
}

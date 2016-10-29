package com.csc108.tradingClient;

import com.csc108.drools.DroolsType;
import com.csc108.drools.DroolsUtility;
import com.csc108.drools.OrderMessage;
import com.csc108.model.fix.InitiatorApplication;
import com.csc108.model.fix.SessionPool;
import com.csc108.utility.FixMsgHelper;
import quickfix.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReject;
import quickfix.fix42.OrderPauseResumeRequest;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zhangbaoshan on 2016/10/25.
 */
public class SendOrder {
    private final static CountDownLatch gateway = new CountDownLatch(1);
    private static TradeClientInitiatorApp initiatorApp;
    private static ThreadedSocketInitiator initiator;

    private static void startInitiator() throws Exception {
        SessionSettings settings = new SessionSettings("configuration/clientConfig.cfg");
        MemoryStoreFactory fileStoreFactory = new MemoryStoreFactory();
        FileLogFactory logFactory = new FileLogFactory(settings);
        DefaultMessageFactory defaultMessageFactory = new DefaultMessageFactory();
        initiatorApp = new TradeClientInitiatorApp();
        initiator = new ThreadedSocketInitiator(initiatorApp, fileStoreFactory, settings,
                logFactory, defaultMessageFactory);
        initiator.start();
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0 || args == null) {
            System.out.println("Paramters must be provided!");
            return;
        }

        startInitiator();

        if (args[0].equals("q")) {
            gateway.countDown();
        }

        gateway.await();
        System.out.println("Existing application ...");
        initiator.stop();

    }

}

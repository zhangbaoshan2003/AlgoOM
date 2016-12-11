package com.csc108;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.concurrent.EventDispatcher;
import com.csc108.drools.DroolsType;
import com.csc108.drools.DroolsUtility;
import com.csc108.log.LogFactory;
import com.csc108.model.cache.*;
import com.csc108.model.fixModel.AcceptorApplication;
import com.csc108.model.fixModel.InitiatorApplication;
import com.csc108.monitor.MonitorServer;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import com.csc108.utility.AlertManager;
import com.csc108.utility.DateTimeUtil;
import org.apache.commons.io.FileUtils;
import quickfix.*;

import java.io.File;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/4/28.
 */
public class Main {
    private final static CountDownLatch gateway=new CountDownLatch(1);
    private static final long startTime = System.currentTimeMillis();

    private static ThreadedSocketAcceptor OmAcceptor;
    private static ThreadedSocketInitiator omInitiator;

    private static AcceptorApplication omAcceptorApplication;
    private static InitiatorApplication omInitiatorApplication;

    private static void startAcceptor() throws Exception {
        omAcceptorApplication = new AcceptorApplication();

        SessionSettings settings = new SessionSettings("configuration/acceptor.cfg");
        MemoryStoreFactory fileStoreFactory= new MemoryStoreFactory();
        FileLogFactory logFactory = new FileLogFactory(settings);
        DefaultMessageFactory defaultMessageFactory = new DefaultMessageFactory();
        OmAcceptor = new ThreadedSocketAcceptor(omAcceptorApplication,fileStoreFactory,settings,
                logFactory,defaultMessageFactory);
        OmAcceptor.start();
    }

    private static void startInitiator () throws Exception{

        SessionSettings settings = new SessionSettings("configuration/initiator.cfg");
        MemoryStoreFactory fileStoreFactory= new MemoryStoreFactory();
        FileLogFactory logFactory = new FileLogFactory(settings);
        DefaultMessageFactory defaultMessageFactory = new DefaultMessageFactory();
        omInitiatorApplication=new InitiatorApplication();
        omInitiator = new ThreadedSocketInitiator(omInitiatorApplication,fileStoreFactory,settings,
                logFactory,defaultMessageFactory);
        omInitiator.start();
    }

    public static void main(String[] args)  {
        try{
            //clear log
            System.out.println("Clear log ...");
            try{
                FileUtils.cleanDirectory(new File("orders"));
                FileUtils.cleanDirectory(new File("log"));
                FileUtils.cleanDirectory(new File("Syslog"));
                FileUtils.cleanDirectory(new File("store"));

            }catch (Exception ex){
                System.out.println("Clear log error @ "+ex.getStackTrace());
            }

            LogFactory.info("Begin initializing ...");
            LogFactory.info("Starting event dispatcher ...");
            EventDispatcher.getInstance().start();

            try {
                System.out.printf("Initializing trading rules ... %n");
                TradingRuleProvider.getInstance().initialize("NormalTradingRule.xml");
            }catch (Exception ex){
                System.err.printf("Error initializing trading rules ... %n %s ",ex);
                System.exit(-1);
            }

            System.out.println("Initializing micro structure ... ");
            try{
                MicroStructureDataManager.getInstance().initialize();
            }catch (Exception ex){
                System.exit(-1);
            }

            System.out.println("Initializing issue type ... ");
            try{
                IssueTypeDataManager.getInstance().initialize();
            }catch (Exception ex){
                System.exit(-1);
            }

            if(GlobalConfig.isActive_mq_available()==true){
                try{
                    System.out.println("Initializing market data active mq ...");
                    RealTimeDataManager.getInstance().initialize();
                    OrderbookDataManager.getInstance().initialize();
                }catch (Exception ex){
                    com.csc108.log.LogFactory.error("Initializing market data active mq error!",ex);
                    System.exit(-1);;
                }

                try{
                    System.out.println("Initializing alert active mq ...");
                    AlertManager.getInstance().init();
                }catch (Exception ex){
                    com.csc108.log.LogFactory.error("Initializing alert active mq error!",ex);
                    System.exit(-1);;
                }

                try{
                    System.out.println("Initializing trade data active mq ...");
                    TradeDataMqManager.getInstance().initialize();
                }catch (Exception ex){
                    com.csc108.log.LogFactory.error("Initializing trade data active mq error!",ex);
                    System.exit(-1);;
                }
            }

            try{
                System.out.println("Initializing drools rules ...");
                DroolsUtility.init(GlobalConfig.getNewSingleOrderRuleFiles(), DroolsType.NEW_SINGLE_ORDER_REQUEST);
                DroolsUtility.init(GlobalConfig.getCancelRejectRuleFiles(), DroolsType.CANCEL_REJECTED);
                DroolsUtility.init(GlobalConfig.getExecutionReportRuleFiles(), DroolsType.EXECUTION_REPORT);
                DroolsUtility.init(GlobalConfig.getCancelRequestRuleFiles(), DroolsType.CANCEL_REQUEST);

            }catch (Exception ex){
                com.csc108.log.LogFactory.error("Initialize drools error!",ex);
                System.exit(-1);;
            }

            System.out.println("Starting acceptor ...");
            try{
                startAcceptor();
            }catch (Exception ex){
                com.csc108.log.LogFactory.error("Start up acceptor error!",ex);
                System.exit(-1);;
            }

            System.out.println("Starting initiator ...");
            try{
                startInitiator();
            }catch (Exception ex){
                com.csc108.log.LogFactory.error("Start up initiator error!",ex);
               System.exit(-1);
            }

            try{
                System.out.println("Start up OMS monitor ...");
                MonitorServer.ServerStart(GlobalConfig.getMonitorIP(), GlobalConfig.getMonitorPort());
            }catch (Exception ex){
                com.csc108.log.LogFactory.error("Start up OMS monitor error!",ex);
                System.exit(-1);;
            }

            System.out.println("Algo OM started successfully ....");

            //set up timeout to exist
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    long minutesDifference = DateTimeUtil.getMinutesDifference(startTime, System.currentTimeMillis());
                    com.csc108.log.LogFactory.info(String.format("Heartbeat @ %s", DateTimeUtil.time2Str(LocalTime.now())));
                    if (minutesDifference >= GlobalConfig.getTimeOutInMinutes()) {
                        com.csc108.log.LogFactory.info(String.format("Time out to exit @ %s", DateTimeUtil.time2Str(LocalTime.now())));
                        gateway.countDown();
                    }
                }
            }, 0, 10, TimeUnit.SECONDS);

            gateway.await();

            try{
                System.out.println("Close monitor ...");
                MonitorServer.shutdown();
            }catch (Exception ex){
                LogFactory.error("Error on close monitor!",ex);
            }

            try{
                System.out.println("Close disruptor pool ...");
                EventDispatcher.getInstance().stop();
            }catch (Exception ex){
                LogFactory.error("Error on Close disruptor pool!", ex);
            }

            try{
                System.out.println("Close alert mq ...");
                AlertManager.getInstance().stop();
            }catch (Exception ex){
                LogFactory.error("Close alert mq !", ex);
            }

            try{
                System.out.println("Close orderbook data subscriber ...");
                OrderbookDataManager.getInstance().stop();
            }catch (Exception ex){
                LogFactory.error("Close orderbook data subscriber error!", ex);
            }

            try{
                System.out.println("Close real time data subscriber ...");
                RealTimeDataManager.getInstance().stop();
            }catch (Exception ex){
                LogFactory.error("Close real time data subscriber error!", ex);
            }

            try{
                System.out.println("Close trade data subscriber ...");
                TradeDataMqManager.getInstance().stop();
            }catch (Exception ex){
                LogFactory.error("Close trade data subscriber error!", ex);
            }

            try{
                System.out.println("Closing acceptor ...");
                OmAcceptor.stop(true);
            }catch (Exception ex){
                LogFactory.error("Closing acceptor error@",ex);
            }

            try{
                System.out.println("Closing initiator ...");
                omInitiator.stop(true);
            }catch (Exception ex){
                LogFactory.error("Closing initiator error@",ex);
            }

            System.exit(0);


        }catch (Exception ex){
            System.out.println("Error happened in main method!@"+ex);

            System.exit(-1);
        }
    }
}

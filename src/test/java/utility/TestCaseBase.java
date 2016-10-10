package utility;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.concurrent.EventDispatcher;
import com.csc108.drools.DroolsType;
import com.csc108.drools.DroolsUtility;
import com.csc108.log.*;
import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.cache.RealTimeDataManager;
import com.csc108.model.cache.TradeDataMqManager;
import com.csc108.model.fix.AcceptorApplication;
import com.csc108.model.fix.InitiatorApplication;
import com.csc108.model.fix.SessionPool;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.ExchangeOrder;
import com.csc108.utility.AlertManager;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import quickfix.*;
import utility.ClientApplication;
import utility.ExchangeApplication;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/5/7.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCaseBase  extends TestCase {
    private GlobalConfig config;

    private static ThreadedSocketInitiator clientInitiator;
    private static ThreadedSocketAcceptor OmAcceptor;
    private static ThreadedSocketInitiator omInitiator;
    private static ThreadedSocketAcceptor exgAcceptor;

    protected static boolean kickoff=false;
    protected static boolean wrapup=false;

    protected static SessionID omAcceptorSessionID;
    protected static SessionID clientSessionID;
    protected static SessionID omInitiatorSessionID;
    protected static SessionID exgAppSessionID;

    protected static final CountDownLatch initiatorLogonLatch = new CountDownLatch(1);
    private static final CountDownLatch  simulatorLogonLatch = new CountDownLatch(1);
    private static ConcurrentHashMap<String,ClientOrder> orderSet = new ConcurrentHashMap<>();

    protected static ClientApplication clientApplication;
    protected static AcceptorApplication omAcceptorApplication;
    protected static ExchangeApplication exchangeApplication;
    protected static InitiatorApplication omInitiatorApplication;

    protected EventDispatcher dispatcher = EventDispatcher.getInstance();

    protected TestFixMsgHelper fixMsgHelper = TestFixMsgHelper.Instance;

    protected void clearLog(){
        try{
            FileUtils.cleanDirectory(new File("orders"));
            FileUtils.cleanDirectory(new File("log"));
            FileUtils.cleanDirectory(new File("Syslog"));
            FileUtils.cleanDirectory(new File("store"));

        }catch (Exception ex){
            System.out.println(ex);
        }
    }



    @Override
    protected void setUp() throws Exception {
        //com.csc108.log.LogFactory.info("Begin initializing ...");

        if(kickoff==true)
            return;

        clearLog();

        if(GlobalConfig.isActive_mq_available()==true){
            try{
                System.out.println("Initializing market data active mq ...");
                //MarketDataManager.getInstance().init();
                OrderbookDataManager.getInstance().initialize();
                RealTimeDataManager.getInstance().initialize();
            }catch (Exception ex){
                com.csc108.log.LogFactory.error("Initializing market data active mq error!",ex);
                throw ex;
            }

            try{
                System.out.println("Initializing alert active mq ...");
                AlertManager.getInstance().init();
            }catch (Exception ex){
                com.csc108.log.LogFactory.error("Initializing alert active mq error!",ex);
                throw ex;
            }

            try{
                System.out.println("Initializing trade data active mq ...");
                TradeDataMqManager.getInstance().initialize();
            }catch (Exception ex){
                com.csc108.log.LogFactory.error("Initializing trade data active mq error!",ex);
                throw ex;
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
            throw ex;
        }

        dispatcher.start();

        long begin =System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString();
        long end =System.currentTimeMillis();
        System.out.printf("UUID %s const %d  %n",uuid,end-begin);

        kickoff=true;
        int sessionPoolSize= SessionPool.getInstance().getClientSessions().size();
        System.out.println("Up stream session pool size:" + sessionPoolSize);

        //GlobalConfig.setupReleaseMode(false);
        logon();

//        DroolsUtility.init(GlobalConfig.getNewSingleOrderRuleFiles(), DroolsType.NEW_SINGLE_ORDER_REQUEST);
//        DroolsUtility.init(GlobalConfig.getCancelRequestRuleFiles(), DroolsType.CANCEL_REQUEST);
//        DroolsUtility.init(GlobalConfig.getExecutionReportRuleFiles(), DroolsType.EXECUTION_REPORT);
//        DroolsUtility.init(GlobalConfig.getCancelRejectRuleFiles(), DroolsType.CANCEL_REJECTED);
    }

    @Override
    protected void tearDown() throws Exception {
        exchangeApplication.orderSet.clear();

        if(wrapup==true){
            clientInitiator.stop();
            OmAcceptor.stop();
            exgAcceptor.stop();
            omInitiator.stop();
            dispatcher.stop();
        }
    }

    private SessionSettings createAcceptorSessionSettings(SessionID sessionID,String port){
        SessionSettings settings = new SessionSettings();
        HashMap<Object, Object> defaults = new HashMap<Object, Object>();
        defaults.put("ConnectionType", "acceptor");
        defaults.put("StartTime", "00:00:00");
        defaults.put("EndTime", "00:00:00");
        defaults.put(SessionSettings.SENDERCOMPID, sessionID.getSenderCompID());
        defaults.put(SessionSettings.TARGETCOMPID, sessionID.getTargetCompID());
        defaults.put("BeginString", "FIX.4.2");
        defaults.put("ResetOnDisconnect", "N");
        defaults.put("FileStorePath", "store");
        defaults.put("FileLogPath", "log");
        defaults.put("ResetOnLogon", "Y"); //TimeZone=Asia/Chungking
        //defaults.put("TimeZone", "Asia/Chungking");
        defaults.put("ValidateUserDefinedFields", "N");
        defaults.put("UseDataDictionary", "Y");

        settings.set(defaults);
        settings.setString(sessionID, "SocketAcceptPort", port);
        settings.setString(sessionID, "DataDictionary", "configuration/FIX_ALGO.xml");

        settings.setString(sessionID, "SocketAcceptPort", port);

        defaults.put("RejectInvalidMessage", "N");
        defaults.put("DataDictionary", "configuration/FIX_ALGO.xml");

        return settings;
    }

    private SessionSettings createInitiatorSessionSettings(SessionID sessionID,String port){
        SessionSettings settings = new SessionSettings();
        HashMap<Object, Object> defaults = new HashMap<Object, Object>();
        defaults.put("ConnectionType", "initiator");
        //defaults.put("SocketConnectProtocol", ProtocolFactory.getTypeString(ProtocolFactory.SOCKET));
        defaults.put("SocketConnectHost", "localhost");
        defaults.put("SocketConnectPort", port);
        defaults.put("StartTime", "00:00:00");
        defaults.put("EndTime", "00:00:00");
        defaults.put("HeartBtInt", "30");
        defaults.put("ReconnectInterval", "2");
        defaults.put("ResetOnLogon", "Y");
        defaults.put("FileStorePath", "store");
        defaults.put("FileLogPath", "log");
        defaults.put("ValidateUserDefinedFields", "N");
        //defaults.put("TimeZone", "Asia/Chungking");
        defaults.put("ValidateUserDefinedFields", "N");
        settings.set(defaults);
        settings.setString(sessionID, "BeginString", FixVersions.BEGINSTRING_FIX42);
        settings.setString(sessionID, "DataDictionary", "configuration/FIX_ALGO.xml");

        defaults.put("RejectInvalidMessage", "N");
        defaults.put("DataDictionary", "configuration/FIX_ALGO.xml");
        defaults.put("UseDataDictionary", "Y");

        return settings;
    }

    private void logon() throws Exception {
        //prepare upstream client app
        if(clientApplication!=null)
            return;

        omAcceptorApplication = new AcceptorApplication();
        omAcceptorSessionID =  new SessionID(FixVersions.BEGINSTRING_FIX42,"OM","BANZAI");
        SessionSettings settings = createAcceptorSessionSettings(omAcceptorSessionID,"6001");
        FileLogFactory logFactory = new FileLogFactory(settings);
        OmAcceptor=new ThreadedSocketAcceptor(omAcceptorApplication,new FileStoreFactory(settings),
                settings,logFactory,new DefaultMessageFactory());
        OmAcceptor.start();

        clientApplication = new ClientApplication(initiatorLogonLatch,orderSet);
        clientSessionID = new SessionID(FixVersions.BEGINSTRING_FIX42,"BANZAI","OM");
        settings = createInitiatorSessionSettings(clientSessionID,"6001");
        logFactory = new FileLogFactory(settings);
        clientInitiator = new ThreadedSocketInitiator(clientApplication,new FileStoreFactory(settings),
                settings,logFactory,new DefaultMessageFactory());
        clientInitiator.start();
        boolean loggedOn= initiatorLogonLatch.await(5, TimeUnit.SECONDS);
        if(loggedOn==false){
            assertEquals("Client application not logged on!","1","2");
        }

        Thread.sleep(2000);
        assertEquals(true, Session.lookupSession(omAcceptorSessionID).isLoggedOn());
        assertEquals(true, Session.lookupSession(clientSessionID).isLoggedOn());
        assertEquals(1,SessionPool.getInstance().getClientSessions().size());

        //prepare downstream exchage app
        omInitiatorApplication = new InitiatorApplication();
        omInitiatorSessionID = new SessionID(FixVersions.BEGINSTRING_FIX42,"OM","EXG");
        settings = createInitiatorSessionSettings(omInitiatorSessionID, "7001");
        logFactory = new FileLogFactory(settings);
        omInitiator = new ThreadedSocketInitiator(omInitiatorApplication ,new FileStoreFactory(settings),
                settings,logFactory,new DefaultMessageFactory());
        omInitiator .start();

        exgAppSessionID = new SessionID(FixVersions.BEGINSTRING_FIX42,"EXG","OM");
        exchangeApplication = new ExchangeApplication(simulatorLogonLatch);
        settings = createAcceptorSessionSettings(exgAppSessionID, "7001");
        logFactory = new FileLogFactory(settings);
        exgAcceptor= new ThreadedSocketAcceptor(exchangeApplication,new FileStoreFactory(settings),
                settings,logFactory,new DefaultMessageFactory());
        exgAcceptor.start();

        loggedOn= simulatorLogonLatch.await(5, TimeUnit.SECONDS);
        if(loggedOn==false){
            assertEquals("Exchange application not logged on!","1","2");
        }
        Thread.sleep(2000);
        assertEquals(true, Session.lookupSession(omInitiatorSessionID).isLoggedOn());
        assertEquals(true, Session.lookupSession(exgAppSessionID).isLoggedOn());
    }
}

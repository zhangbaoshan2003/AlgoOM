package model;

import com.csc108.log.LogFactory;
import com.csc108.model.cache.TradeDataMqManager;
import com.csc108.model.fix.SessionPool;
import com.csc108.monitor.command.ClientOrderCommand;
import com.csc108.monitor.command.FixEngineCommand;
import com.csc108.utility.Alert;
import com.csc108.utility.AlertManager;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import quickfix.SessionID;
import utility.TestUtility;

import java.io.File;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangbaoshan on 2016/9/9.
 */
public class SessionPoolTest extends TestCase {
    private final CountDownLatch gate = new CountDownLatch(1);
    @Override
    public void setUp() throws Exception {
        try{
            FileUtils.cleanDirectory(new File("orders"));
            FileUtils.cleanDirectory(new File("log"));
            FileUtils.cleanDirectory(new File("Syslog"));
            FileUtils.cleanDirectory(new File("store"));

        }catch (Exception ex){
            System.out.println(ex);
        }
        AlertManager.getInstance().init();
    }

    public void testAddSession() throws Exception {
        Executor executor = Executors.newFixedThreadPool(4);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SessionID sessionID = new SessionID("FIX4.2", "OM_SG_PROD_08", "ENGINE_PROD_11");
                    gate.await();
                    //final long id = seed1.getAndIncrement();
                    System.out.printf("Thread #%s triggered at %s \n",Thread.currentThread().getId(), LocalTime.now());
                    SessionPool.getInstance().addClientSession(sessionID);
                } catch (Exception ex) {
                    LogFactory.error("Error happened",ex);
                    ex.printStackTrace();
                    System.out.println(ex);
                }
            }
        });
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SessionID sessionID = new SessionID("FIX4.2", "OM_SG_PROD_08", "ENGINE_PROD_10");
                    gate.await();
                    //final long id = seed1.getAndIncrement();
                    System.out.printf("Thread #%s triggered at %s \n",Thread.currentThread().getId(), LocalTime.now());
                    SessionPool.getInstance().addClientSession(sessionID);
                } catch (Exception ex) {
                    LogFactory.error("Error happened",ex);
                    ex.printStackTrace();
                    System.out.println(ex);
                }
            }
        });
        t2.start();

        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SessionID sessionID = new SessionID("FIX4.2", "OM_SG_PROD_08", "ENGINE_PROD_08");
                    gate.await();
                    //final long id = seed1.getAndIncrement();
                    System.out.printf("Thread #%s triggered at %s \n",Thread.currentThread().getId(), LocalTime.now());
                    SessionPool.getInstance().addClientSession(sessionID);
                } catch (Exception ex) {
                    LogFactory.error("Error happened",ex);
                    ex.printStackTrace();
                    System.out.println(ex);
                }
            }
        });
        t3.start();

        Thread t4 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SessionID sessionID = new SessionID("FIX4.2", "OM_SG_PROD_08", "ENGINE_PROD_12");
                    gate.await();
                    //final long id = seed1.getAndIncrement();
                    System.out.printf("Thread #%s triggered at %s \n",Thread.currentThread().getId(), LocalTime.now());
                    SessionPool.getInstance().addClientSession(sessionID);
                } catch (Exception ex) {
                    LogFactory.error("Error happened",ex);
                    ex.printStackTrace();
                    System.out.println(ex);
                }
            }
        });
        t4.start();

        Thread t5 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SessionID sessionID = new SessionID("FIX4.2", "OM_SG_PROD_08", "ENGINE_PROD_09");
                    gate.await();
                    //final long id = seed1.getAndIncrement();
                    System.out.printf("Thread #%s triggered at %s \n",Thread.currentThread().getId(), LocalTime.now());
                    SessionPool.getInstance().addClientSession(sessionID);
                } catch (Exception ex) {
                    LogFactory.error("Error happened",ex);
                    ex.printStackTrace();
                    System.out.println(ex);
                }
            }
        });
        t5.start();


//        for (int i=0;i<32;i++){
//            final int id = i;
//            executor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        SessionID sessionID = new SessionID("FIX4.2", "From_" + id, "To_" + id);
//                        gate.await();
//                        //final long id = seed1.getAndIncrement();
//                        System.out.printf("Thread #%s triggered at %s \n",Thread.currentThread().getId(), LocalTime.now());
//                        SessionPool.getInstance().addClientSession(sessionID);
//                    } catch (Exception ex) {
//                        LogFactory.error("Error happened",ex);
//                        ex.printStackTrace();
//                        System.out.println(ex);
//                    }
//                }
//            });
//        }
//        //Thread.sleep(1000);

        gate.countDown();
        Thread.sleep(1000);
        FixEngineCommand cmd = new FixEngineCommand();
        String result2= cmd.list_all_sessions(new String[]{});
        System.out.printf("%50s",result2);

        assertEquals(5, SessionPool.getInstance().getClientSessions().size());
        //boolean noException = TestUtility.checkFileEmpty("Syslog/Error.log");
        //assertEquals("Exception happened!", true, noException);
    }
}

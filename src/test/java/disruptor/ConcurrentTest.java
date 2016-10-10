package disruptor;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.PerformanceCounter;
import com.csc108.disruptor.concurrent.EventDispatcher;
import com.csc108.disruptor.event.EventType;
import com.csc108.log.LogFactory;
import com.csc108.model.fix.FixEvaluationData;
import quickfix.DataDictionary;
import quickfix.Message;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;
import utility.TestCaseBase;
import utility.TestUtility;

import java.util.concurrent.*;

/**
 * Created by zhangbaoshan on 2016/5/7.
 */
public class ConcurrentTest extends TestCaseBase {

    //Test if thread mapping for same order are correct
    public void testEventDispatch() throws Exception {
        int threadNum= GlobalConfig.getThreadNums();
        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        final CountDownLatch latch = new CountDownLatch(1024);
        for (int i=0;i<latch.getCount();i++){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Message orderNewSingle = fixMsgHelper.buildNewOrderSingleMsg("IBM", new Side(Side.BUY), new OrdType(OrdType.MARKET), 1000, 98.00);
                        dispatcher.dispatchEvent(EventType.NEW_SINGLE_ORDER, new FixEvaluationData(orderNewSingle,omInitiatorSessionID));
                    }catch (Exception ex){
                        LogFactory.error("Error",ex);
                    }finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();

        final CountDownLatch latch2 = new CountDownLatch(1024);
        for (int i=0;i<latch2.getCount();i++){
            final int index=i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        NewOrderSingle newOrderSingle = fixMsgHelper.getNewOrderSingleRequestPool().get(index);
                        OrderCancelRequest cancelOrderRequest= fixMsgHelper.buildCancelRequestMsg(newOrderSingle.getString(11));
                        dispatcher.dispatchEvent(EventType.CANCEL_ORDER_REQUEST, new FixEvaluationData(cancelOrderRequest,omInitiatorSessionID));

                    }catch (Exception ex){
                        LogFactory.error("Error",ex);
                    }finally {
                        latch2.countDown();
                    }
                }
            });
        }
        latch2.await();

        //trigger tear down to fire
        //wrapup=true;

        boolean noException = TestUtility.checkFileEmpty("Syslog/Error.log");
        assertEquals("Exception happened when processing concurrent order!", true, noException);
    }

    public void testPerformance() throws Exception {
        CountDownLatch l = new CountDownLatch(1);
        EventDispatcher.getInstance().start();
        ScheduledExecutorService  scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int threadNum= GlobalConfig.getThreadNums();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.getInstance().dispatchEvent(EventType.PERFORMANCE_TEST,null);
            }
        });


        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    l.countDown();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }, 1, TimeUnit.SECONDS);

        l.await();

        System.out.println(PerformanceCounter.getCounter());

    }
}

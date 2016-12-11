package model;

import com.csc108.model.cache.AlgoTimeSeriesDataCache;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderSnapshot;
import junit.framework.TestCase;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangbaoshan on 2016/9/6.
 */
public class AlgoTimeSeriesCacheTest extends TestCase {
    private final AtomicInteger counter = new AtomicInteger(0);
    private CountDownLatch getway = new CountDownLatch(100);
    private final Random r=new Random(7);

    private final AlgoTimeSeriesDataCache cache = new AlgoTimeSeriesDataCache();
    private void putSnapshot(AlgoTimeSeriesDataCache cache,ClientOrder clientOrder){
        OrderSnapshot snapshot= OrderSnapshot.click(clientOrder);
        cache.put(clientOrder.getClientOrderId(),snapshot);
    }

    public void testRun() throws Exception {
        ClientOrder clientOrder = new ClientOrder();
        clientOrder.setClientOrderId(UUID.randomUUID().toString());

        //putSnapshot(cache,clientOrder);
        //assertEquals(1,cache.get(clientOrder.getClientOrderId()).size());

        Executor executor = Executors.newFixedThreadPool(4);
        for (int i=0;i<4;i++){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        clientOrder.setCumQty(r.nextLong());
                        putSnapshot(cache, clientOrder);
                        getway.countDown();
                        if(getway.getCount()==0)
                            return;
                    }
                }
            });
        }

        getway.await();
        assertEquals(true, cache.get(clientOrder.getClientOrderId()).size() > 10);
        String outPut = cache.outputString(clientOrder.getClientOrderId());
        System.out.println(outPut);
    }
}

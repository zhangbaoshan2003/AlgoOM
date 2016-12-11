package infrastructure;

import com.csc108.model.cache.RealTimeDataManager;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.model.fixModel.order.OrderPool;
import utility.TestCaseBase;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * Created by zhangbaoshan on 2016/8/17.
 */
public class RealTimeMqManagerTest extends TestCaseBase {

    public void testRealTimeDataMqManager() throws Exception {
        String patternStr="^\\d+.\\d+";
        Pattern pattern =Pattern.compile(patternStr);
        assertFalse(pattern.matcher("-").find());
        assertTrue(pattern.matcher("123.589").find());
        assertTrue(pattern.matcher("00123.58900").find());
        assertTrue(pattern.matcher("0012358900").find());
        assertFalse(pattern.matcher("_00123_58900").find());

        String symbol="600198";
        double qty=3500;
        double price=10.10;
        clientApplication.getOrderSet().clear();
        clientApplication.sendNewPeggingSingleOrder(symbol, qty, price);
        TimeUnit.SECONDS.sleep(1);

        //no orderbook, no allocations
        Optional<ClientOrder> clientOrderOptional =  OrderPool.getClientOrderMap().values().stream().findFirst();
        assertEquals(clientOrderOptional.isPresent(), true);
        OrderHandler handler = clientOrderOptional.get().getOrderHandler();
        assertNotNull(handler.getPegConfiguration());
        System.out.println(handler.getPegConfiguration().toString());
        assertTrue(handler.getExchangeOrders().size() == 0);

//        TimeUnit.SECONDS.sleep(1);
//        handler.getClientOrder().setEffectiveTime(LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(),
//                9, 35, 50));
//        handler.getClientOrder().setExpireTime(LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(),
//                14, 5, 50));

        String intervalKey=RealTimeDataManager.getInstance().buildMsgRequestKey(handler, false, -1, -1);
        assertNotNull(intervalKey);

        intervalKey=RealTimeDataManager.getInstance().buildMsgRequestKey(handler, true, -1, -1);
        assertNotNull(intervalKey);

        assertNotNull(handler.getRealTimeMarketData());
        assertNotNull(handler.getIntervalMarketData());
        assertNotNull(handler.getAllDayIntervalMarketData());
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        CountDownLatch gateWay= new CountDownLatch(100);

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("RT:"+handler.getRealTimeMarketData().getDateTime()+"_LP:"+handler.getRealTimeMarketData().getLp());
                System.out.println("Interval:" + handler.getIntervalMarketData().getDateTime()+" getVwp:" + handler.getIntervalMarketData().getVwp());
                System.out.println("All day interval:" + handler.getAllDayIntervalMarketData().getDateTime() + " getVwp:" + handler.getAllDayIntervalMarketData().getVwp());
                gateWay.countDown();
            }
        }, 0, 1, TimeUnit.SECONDS);

        gateWay.await();

    }
}

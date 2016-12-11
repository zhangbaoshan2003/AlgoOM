package infrastructure;

import com.csc108.model.Allocation;
import com.csc108.model.OrderState;
import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.model.fixModel.order.OrderPool;
import com.csc108.model.market.OrderBook;
import utility.TestCaseBase;
import utility.TestUtility;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/8/15.
 */
public class TradeDataMqManagerTest extends TestCaseBase {

    public void testOrderHandlerPublishMsg() throws Exception {
        ArrayList<Allocation> allocations=new ArrayList<>();
        String securityID ="600006";
        OrderbookDataManager.getInstance().subscribeOrderBook(securityID + ".sh", false, null);

        OrderbookDataManager.getInstance().publish_SH_SecurityData(securityID, 10.01, 10.2, 10.03, 10.04, 10.07, 10.05);
        TimeUnit.SECONDS.sleep(1);
        OrderBook obReference = OrderbookDataManager.getInstance().getLatestOrderBook(securityID + ".sh");
        assertNotNull(obReference);

        String symbol="600000";
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

        long start = System.currentTimeMillis();
        for (int i=0;i<100;i++){
            handler.getClientOrder().setClientOrderId("Peg_"+ Integer.toString(i));
            handler.publishMsg(false);
        }
        long stop = System.currentTimeMillis();
        System.out.printf("Total cost %d seconds.", (stop - start));

        TimeUnit.SECONDS.sleep(2);

        start = System.currentTimeMillis();
        for (int i=0;i<100;i++){
            handler.getClientOrder().setClientOrderId("Peg_"+ Integer.toString(i));
            handler.getClientOrder().setOrderState(OrderState.COMPLETED);
            handler.publishMsg(false);
        }
        stop = System.currentTimeMillis();
        System.out.printf("Total round 2 cost %d seconds.", (stop - start));


        boolean noException = TestUtility.checkFileEmpty("Syslog/Error.log");
        assertEquals("Exception happened!", true, noException);
        wrapup=true;
    }

}

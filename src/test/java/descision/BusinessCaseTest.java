package descision;

import com.csc108.log.LogFactory;
import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.fixModel.sessionPool.SessionPool;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.ManuallyOrder;
import com.csc108.model.fixModel.order.OrderPool;
import com.csc108.model.market.OrderBook;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import quickfix.SessionID;
import quickfix.field.OrdStatus;
import utility.TestCaseBase;
import utility.TestPurpose;
import utility.TestUtility;

import java.util.concurrent.*;

/**
 * Created by zhangbaoshan on 2016/5/11.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusinessCaseTest extends TestCaseBase {
    private static SessionID exchangeSessionId=null;

    private void processClientOrderInnerNormally(CountDownLatch mainGateway){
        try {
            int quantity = 10000;
            String symbol = "IBM";
            //step1: receive pending
            //step2: receive new ack
            //step3: partill fill
            //step4: fill
            CountDownLatch stepGateWay = new CountDownLatch(4);
            clientApplication.sendNewSingleOrderThenFilledIt(symbol, quantity, 10.5d, stepGateWay);
            if (stepGateWay.await(1, TimeUnit.SECONDS) == false) {
                assertEquals(true, false);
            }
        } catch (Exception ex) {
            LogFactory.error("Error", ex);
        } finally {
            mainGateway.countDown();
        }
    }

    private void cancelNewOrderNotSentOut(CountDownLatch mainGateWay)  {
        try{
            int quantity = 10000;
            String symbol = "IBM";
            //step1: pending new
            //step2: new ack
            //step3: pending canceled
            CountDownLatch stepGateWay = new CountDownLatch(2);
            clientApplication.cancelClientOrderWithoutSentOutExchangeOrders(symbol, quantity, 10.5d, stepGateWay);

            if(stepGateWay.await(2, TimeUnit.SECONDS)==false){
                assertEquals(true,false);
            }
        }catch (Exception ex){
            LogFactory.error("cancelNewOrderNotSentOut",ex);
        }finally {
            mainGateWay.countDown();
        }

    }

    private void newOrderCancelRejectedThenFilled(CountDownLatch mainGateWay)  {
        try{
            int quantity = 10000;
            String symbol = "IBM";
            //step1: pending new
            //step2: new ack
            //step3: pending canceled
            //step4: cancel rejected
            //step5: filled
            CountDownLatch stepGateWay = new CountDownLatch(1);
            clientApplication.cancelClientOrderWithoutSentOutExchangeOrders(symbol, quantity, 10.5d, stepGateWay);

            if(stepGateWay.await(1, TimeUnit.SECONDS)==false){
                assertEquals(true,false);
            }
        }catch (Exception ex){
            LogFactory.error("cancelNewOrderNotSentOut",ex);
        }finally {
            mainGateWay.countDown();
        }

    }

    private void partialFillThenCanceled(CountDownLatch mainGateway)  {
        int quantity = 10000;
        String symbol = "IBM";

        //step1: pending new
        //step2: new ack
        //step3: partial filled
        //step4: pending cancel
        //step5: canceled
        try{
            CountDownLatch stepGateWay = new CountDownLatch(5);
            clientApplication.cancelClientOrderWithoutSentOutExchangeOrders(symbol, quantity, 10.5d, stepGateWay);

            if(stepGateWay.await(2, TimeUnit.SECONDS)==false){
                assertEquals(true,false);
            }
        }catch (Exception ex){
            LogFactory.error("test error!",ex);
        }finally {
            mainGateway.countDown();
        }
    }

    //region algo engine test

    /*Verify procedure: new->new ack-partil fill->fill*/
    public void test03NewOrderSingleRequestProcessTest() throws Exception{

        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();

        TestUtility.Purpose= TestPurpose.FULL_FILL;

        Executor executor = Executors.newFixedThreadPool(4);
        CountDownLatch mainGateway=new CountDownLatch(100);

        for(int i=0;i<mainGateway.getCount();i++){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    processClientOrderInnerNormally(mainGateway);
                }
            });
        }

        if(mainGateway.await(10,TimeUnit.SECONDS)==false){
            assertEquals(true,false);
        }
        Thread.sleep(1000);

        long totalNotFilled= clientApplication.getOrderSet().values().stream().filter(x -> Double.compare(x.getLeavesQty(),0)!=0)
                .count();
        if(totalNotFilled!=0){
            String clientOrid =clientApplication.getOrderSet().values().stream().filter(x -> Double.compare(x.getLeavesQty(), 0) != 0).findAny().get().getClientOrderId();
            System.out.printf("%10s[%s]%-10s","-",clientOrid,"-");
        }

        assertEquals(0, totalNotFilled);
    }

    /* cancel request -> canceled directly */
    public void test04CanceledDirectlyWhenNoExchangeOrderSent(){
        try{
            clientApplication.getOrderSet().clear();
            CountDownLatch mainGateway = new CountDownLatch(100);
            Executor executor = Executors.newFixedThreadPool(4);

            //TestUtility.IS_FULLY_FILL=false;
            //TestUtility.IS_PARTIALLY_FILL=false;

            //simulate no available exchange session
            exchangeSessionId  = SessionPool.getInstance().getAlgoExchangeSessions().get(0);
            SessionPool.getInstance().getAlgoExchangeSessions().clear();
            Thread.sleep(100);

            for(int i=0;i<mainGateway.getCount();i++){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        cancelNewOrderNotSentOut(mainGateway);
                    }
                });
            }

            if(mainGateway.await(10,TimeUnit.SECONDS)==false){
                assertEquals(true,false);
            }
        }catch (Exception ex){
            LogFactory.error("test cancel directly error!",ex);
        }
    }

    public void test05NewOrderPartialFilledThenCanceled() throws Exception{
        CountDownLatch mainGateway = new CountDownLatch(400);
        Executor executor = Executors.newFixedThreadPool(4);

        clientApplication.getOrderSet().clear();
        TestUtility.Purpose = TestPurpose.PARTIAL_FILL_THEN_CANCEL;

        if(SessionPool.getInstance().getAlgoExchangeSessions().size()==0 &&
                exchangeSessionId!=null){
            SessionPool.getInstance().getAlgoExchangeSessions().add(exchangeSessionId);
        }

        for(int i=0;i<mainGateway.getCount();i++){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    partialFillThenCanceled(mainGateway);
                }
            });
        }

        if(mainGateway.await(20,TimeUnit.SECONDS)==false){
            assertEquals(true, false);
        }

        long totalNotFilled= clientApplication.getOrderSet().values().stream().filter(x -> x.getOrdStatus().getValue() != OrdStatus.CANCELED)
                .count();
        assertEquals(0, totalNotFilled);
        assertEquals(400, clientApplication.getOrderSet().size());
    }

    public void test06CancelRejected() throws Exception{
        clientApplication.getOrderSet().clear();
        TestUtility.Purpose = TestPurpose.CANCEL_REJECTED;

        if(SessionPool.getInstance().getAlgoExchangeSessions().size()==0 &&
                exchangeSessionId!=null){
            SessionPool.getInstance().getAlgoExchangeSessions().add(exchangeSessionId);
        }

        CountDownLatch mainGateway = new CountDownLatch(1);
        newOrderCancelRejectedThenFilled(mainGateway);
        mainGateway.await();
    }

    public void test07MarketDataTriggeredPause() throws Exception {
        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();

        OrderbookDataManager.getInstance().subscribeOrderBook("000300.sh",false,null);

        //set 000300.sh pre close to trigger pause condition
        //MarketDataManager.getInstance().simulateMarketDataForTestPurpose("000300", Double.NaN, Double.NaN);
        OrderbookDataManager.getInstance().publishData("000300", Double.NaN, Double.NaN);
        TimeUnit.SECONDS.sleep(1);
        OrderBook orderBook= OrderbookDataManager.getInstance().getLatestOrderBook("000300.sh");
        //assertEquals(Double.NaN, orderBook.getPreClose());

        //simulate a conditional client order
        CountDownLatch gateWay=new CountDownLatch(1);
        ClientOrder clientOrder = clientApplication.sendNewConditionalOrder("600000.sh", 5000, 10.2, gateWay);
        TimeUnit.SECONDS.sleep(1);

        //sicne there is no market data for reference 000300, not exchange order since pause condition triggered
        assertEquals(0, exchangeApplication.getOrderSet().size());


        for (int i=0;i<5;i++){
            //keep simualte a pause condtion 000300.sh's last price < 3000
            OrderbookDataManager.getInstance().publishData("000300", 2000, 5000+i);
            assertEquals(0, exchangeApplication.getOrderSet().size());
            TimeUnit.SECONDS.sleep(1);
            orderBook= OrderbookDataManager.getInstance().getLatestOrderBook("000300.sh");
            System.out.printf("000300 last price %f\n", orderBook.getLastPrice());
        }

        //simulate a dummy reference order to trigger pause/resume condition and sent out
        double laxPrice= 3000;
        OrderbookDataManager.getInstance().publishData("000300", 3100, laxPrice);
        TimeUnit.SECONDS.sleep(1);

        orderBook= OrderbookDataManager.getInstance().getLatestOrderBook("000300.sh");
        assertEquals(laxPrice, orderBook.getLastPrice());

        System.out.printf("000300 last price %f\n", orderBook.getLastPrice());
        TimeUnit.SECONDS.sleep(1);
        assertEquals(1, exchangeApplication.getOrderSet().size());
    }

    public void test002CancelWithIncorrectQuantity() throws Exception {
        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();
        TestUtility.Purpose = TestPurpose.CANCEL_THEN_PARTIAL_FILL;

        //send a new order from client
        // should involve 5 steps :pending new->new ack->pending cancel->canceled->partial fill
        CountDownLatch gateWay=new CountDownLatch(5);
        ClientOrder clientOrder = clientApplication.sendNewSingleOrderThenFilledIt("600000.sh", 5000, 10.2, gateWay);
        Thread.sleep(100);
        clientApplication.cancelOrder(clientOrder);
        if(gateWay.await(5,TimeUnit.SECONDS)==false){
            throw new AssertionError("Failed to process cancel then partial fill process");
        }
        assertTrue(clientOrder.getOrdStatus().getValue()==OrdStatus.CANCELED);
    }

    public void test08CancelDirectlyFromExchange() throws Exception {
        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();
        TestUtility.Purpose = TestPurpose.CANCEL_DIRECTLY_FROM_EXG;

        //send a new order from client
        // should involve 4 steps :pending new->new ack->partial fill->canceled
        CountDownLatch gateWay=new CountDownLatch(4);
        ClientOrder clientOrder = clientApplication.sendNewSingleOrderThenFilledIt("600000.sh", 5000, 10.2, gateWay);
        Thread.sleep(100);
        if(gateWay.await(500,TimeUnit.SECONDS)==false){
            throw new AssertionError("Failed to process cancel then partial fill process");
        }
        assertTrue(clientOrder.getOrdStatus().getValue() == OrdStatus.CANCELED);
    }

    //endregion

    //verify when reevaluate event handled, the order will not be over allocated
    public void test001OrderbookChangedNotOverallolcatedOrder() throws Exception {
        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();

        TestUtility.Purpose = TestPurpose.CANCEL_THEN_PARTIAL_FILL;
        ClientOrder clientOrder = clientApplication.sendNewSingleOrder("600000", 5000, 10.2, null);

        TimeUnit.SECONDS.sleep(1);
        OrderbookDataManager.getInstance().publishData("600000", 15.1, 15.3);
    }

    public void test002ManuallyOrderCancelQtyWrong() throws Exception {
        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();
        OrderPool.getManuallyOrderMap().clear();

        TestUtility.Purpose = TestPurpose.MANUALLY_ORDER_CANCEL_THEN_PARTIAL_FILL;

        String symbol = "600000";
        double price=10.1;
        double qty=10000;

        ClientOrder order = clientApplication.sendNewSingleOrder(symbol, qty, price,null);
        TimeUnit.SECONDS.sleep(1);
        assertEquals(order.getOrdStatus().getValue(), OrdStatus.NEW);
        assertTrue(OrderPool.getManuallyOrderMap().size() > 0);
        ManuallyOrder manuallyOrder = OrderPool.getManuallyOrderMap().values().stream().findAny().get();
        assertNotNull(manuallyOrder);

        assertEquals(manuallyOrder.getLeavsQty(), manuallyOrder.getOrderQty() - manuallyOrder.getCumQty());
    }

    public void test005CancelRejectWithoutCancelRequestFromExchange() throws Exception {
        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();
        OrderPool.getManuallyOrderMap().clear();

        TestUtility.Purpose = TestPurpose.CANCEL_REJECT_THEN_FILL_DIRECTLY_FROM_EXG;

        String symbol = "600000";
        double price=10.1;
        double qty=10000;

        ClientOrder order = clientApplication.sendNewSingleOrder(symbol, qty, price, null);
        TimeUnit.SECONDS.sleep(1);
        assertEquals(order.getOrdStatus().getValue(), OrdStatus.FILLED);

    }

    public void test007ManuallyOrderFilledThenPartialFilled() throws Exception {
        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();
        OrderPool.getManuallyOrderMap().clear();

        TestUtility.Purpose = TestPurpose.MANUALLY_ORDER_FILLED_THEN_PARTIAL_FILL;

        String symbol = "600000";
        double price=10.1;
        double qty=10000;

        ClientOrder order = clientApplication.sendNewSingleOrder(symbol, qty, price,null);
        TimeUnit.SECONDS.sleep(1);
        assertEquals(order.getOrdStatus().getValue(), OrdStatus.NEW);
        assertTrue(OrderPool.getManuallyOrderMap().size() > 0);
        ManuallyOrder manuallyOrder = OrderPool.getManuallyOrderMap().values().stream().findAny().get();
        assertNotNull(manuallyOrder);

        //assertEquals(0, manuallyOrder.getLeavsQty());
        assertEquals(manuallyOrder.getOrdStatus().getValue(), OrdStatus.FILLED);
    }

    public void test99Wrapup(){
        boolean noException = TestUtility.checkFileEmpty("Syslog/Error.log");
        assertEquals("Exception happened!", true, noException);
        wrapup=true;
    }

}

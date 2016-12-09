package descision;

import com.csc108.configuration.GlobalConfig;
import com.csc108.decision.pegging.PeggingDecision;
import com.csc108.model.Allocation;
import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.fix.sessionPool.SessionPool;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.ExchangeOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.model.fix.order.OrderPool;
import com.csc108.model.market.OrderBook;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import quickfix.field.OrdStatus;
import quickfix.field.Side;
import utility.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/8/12.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PeggingOrderTest extends TestCaseBase {

    @Override
    protected void setUp() throws Exception {
        GlobalConfig.setCounterCompId("EXG");
        super.setUp();
    }

    //region pegging test
    public void test001PeggingDescition() throws Exception {
        ArrayList<Allocation> allocations=new ArrayList<>();
        String securityID ="600006";
        OrderbookDataManager.getInstance().subscribeOrderBook(securityID + ".sh", false, null);

        OrderbookDataManager.getInstance().publish_SH_SecurityData(securityID, 10.01, 10.2, 10.03, 10.04, 10.07, 10.05);
        TimeUnit.SECONDS.sleep(1);
        OrderBook obReference = OrderbookDataManager.getInstance().getLatestOrderBook(securityID + ".sh");
        assertNotNull(obReference);

        PeggingDecision decision = new PeggingDecision();
        decision.peggingAllocate(allocations, 3500, 3, 1000, new Side(Side.BUY), obReference, 100);
        assertEquals(3, allocations.size());
        assertEquals(1000.0, allocations.get(2).getAllocatedQuantity());
        assertEquals(10.03, allocations.get(2).getAllocatedPrice());

        allocations.clear();
        decision.peggingAllocate(allocations, 2500, 3, 1000, new Side(Side.BUY), obReference, 100);
        assertEquals(3, allocations.size());
        assertEquals(900.0, allocations.get(0).getAllocatedQuantity());
        assertEquals(800.0,allocations.get(1).getAllocatedQuantity());
        assertEquals(800.0, allocations.get(2).getAllocatedQuantity());

        allocations.clear();
        decision.peggingAllocate(allocations, 1700, 3, 1000, new Side(Side.BUY), obReference, 100);
        assertEquals(3, allocations.size());
        assertEquals(600.0,allocations.get(0).getAllocatedQuantity());
        assertEquals(600.0,allocations.get(1).getAllocatedQuantity());
        assertEquals(500.0, allocations.get(2).getAllocatedQuantity());

        allocations.clear();
        decision.peggingAllocate(allocations, 200, 3, 1000, new Side(Side.BUY), obReference, 100);
        assertEquals(2, allocations.size());
        assertEquals(100.0,allocations.get(0).getAllocatedQuantity());
        assertEquals(100.0,allocations.get(1).getAllocatedQuantity());

        allocations.clear();
        decision.peggingAllocate(allocations, 1300, 3, 1000, new Side(Side.BUY), obReference, 100);
        assertEquals(3, allocations.size());
        assertEquals(500.0, allocations.get(0).getAllocatedQuantity());
        assertEquals(400.0,allocations.get(1).getAllocatedQuantity());
        assertEquals(400.0,allocations.get(2).getAllocatedQuantity());

        allocations.clear();
        decision.peggingAllocate(allocations, 1300, 5, 1000, new Side(Side.BUY), obReference, 100);
        assertEquals(5, allocations.size());
        assertEquals(300.0,allocations.get(0).getAllocatedQuantity());
        assertEquals(300.0,allocations.get(1).getAllocatedQuantity());
        assertEquals(300.0,allocations.get(2).getAllocatedQuantity());
        assertEquals(200.0,allocations.get(3).getAllocatedQuantity());
        assertEquals(200.0,allocations.get(4).getAllocatedQuantity());

    }

    public void test004NoPeggingAllocationNoOrderBook() throws Exception {
        clientApplication.getOrderSet().clear();
        exchangeApplication.getOrderSet().clear();
        OrderPool.getClientOrderMap().clear();
        OrderPool.getExchangeOrderMap().clear();

        String symbol="600000";
        double qty=3500;
        double price=10.10;

        //no order book by default
        OrderBook obReference = OrderbookDataManager.getInstance().getLatestOrderBook(symbol + ".sh");
        assertNull(obReference);

        clientApplication.getOrderSet().clear();
        clientApplication.sendNewPeggingSingleOrder(symbol, qty, price);
        TimeUnit.SECONDS.sleep(1);

        //no orderbook, no allocations
        Optional<ClientOrder> clientOrderOptional =  OrderPool.getClientOrderMap().values().stream().findFirst();
        assertEquals(clientOrderOptional.isPresent(), true);

        OrderHandler handler = clientOrderOptional.get().getOrderHandler();
        assertNotNull(handler.getPegConfiguration());

        System.out.println(handler.getPegConfiguration().toString());
        assertEquals(0, handler.getExchangeOrders().size());

        //simulate orderbook, should trigger allocation
        OrderbookDataManager.getInstance().subscribeOrderBook(symbol + ".sh", false, null);
        OrderbookDataManager.getInstance().publish_SH_SecurityData(symbol, 10.01, 10.2, 10.03, 10.04, 10.07, 10.05);
        TimeUnit.SECONDS.sleep(1);
        obReference = OrderbookDataManager.getInstance().getLatestOrderBook(symbol + ".sh");
        assertNotNull(obReference);

        //TimeUnit.SECONDS.sleep(1000);

        assertEquals(3, handler.getExchangeOrders().size());
        assertEquals(3, exchangeApplication.getOrderSet().size());

        //simualte another orderbook, should cancel one of exchange order
        TestUtility.Purpose =TestPurpose.PEGGING_CANCEL;
        OrderbookDataManager.getInstance().publish_SH_SecurityData(symbol, 10.01, 10.2, 10.03, 10.04, 10.07, 10.04);
        TimeUnit.SECONDS.sleep(1);
        Optional<ExchangeOrder> exchangeOrderToCancelOptional= exchangeApplication.getOrderSet().values().stream().filter(x->x.getPrice()==10.05).findAny();
        assertTrue(exchangeOrderToCancelOptional.isPresent());
        ExchangeOrder exchangeOrderToCancel = exchangeOrderToCancelOptional.get();
        assertEquals(OrdStatus.CANCELED, exchangeOrderToCancel.getOrdStatus().getValue());

        ExchangeOrder exchangeOrderSentToExchange=
                OrderPool.getExchangeOrderMap().values().stream().filter(x->x.getPrice()==10.05).findAny().get();
        assertNotNull(exchangeOrderSentToExchange);
        assertEquals(OrdStatus.CANCELED,exchangeOrderSentToExchange.getOrdStatus().getValue());

        //canceled event should trigger reevaluate logic and generate a new exchange order
        assertEquals(4,OrderPool.getExchangeOrderMap().size());
    }
    //endregion




}

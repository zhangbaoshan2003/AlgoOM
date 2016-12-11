package descision;

import com.csc108.model.Allocation;
import com.csc108.model.AllocationCategory;
import com.csc108.model.AllocationDecisionType;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.ExchangeOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import junit.framework.TestCase;
import quickfix.field.ClOrdID;
import quickfix.fix42.NewOrderSingle;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/8/9.
 */
public class AllocationMechenismTest extends TestCase {

    public void testAllocationToExchangeOrder() throws Exception {
        NewOrderSingle newOrderSingle = new NewOrderSingle();

        newOrderSingle.set(new ClOrdID(UUID.randomUUID().toString()));
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,null);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);

        ArrayList<Allocation> allocations = new ArrayList<>();
        Allocation allocation = new Allocation();
        allocation.setAllocatedQuantity(900);
        allocation.setAllocatedPrice(10.03);
        allocations.add(allocation);

        allocation = new Allocation();
        allocation.setAllocatedQuantity(800);
        allocation.setAllocatedPrice(10.02);
        allocations.add(allocation);

        allocation = new Allocation();
        allocation.setAllocatedQuantity(700);
        allocation.setAllocatedPrice(10.01);
        allocations.add(allocation);

        ArrayList<ExchangeOrder> exchangeOrdersExisted = new ArrayList<>();
        ExchangeOrder exchangeOrder = new ExchangeOrder(null,null,900.0,10.03, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        exchangeOrder = new ExchangeOrder(null,null,800.0,10.02, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        exchangeOrder = new ExchangeOrder(null,null,700.0,10.01, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        ArrayList<Allocation> exchangeOrdersToCreate = new ArrayList<>();
        ArrayList<ExchangeOrder> exchangeOrdersToCancel = new ArrayList<>();
        ArrayList<String> logLines = new ArrayList<>();

        orderHandler.assignNewCancelExchangeOrders(exchangeOrdersToCreate, exchangeOrdersToCancel,exchangeOrdersExisted, allocations, logLines);

        assertEquals(0, exchangeOrdersToCreate.size());
        assertEquals(0,exchangeOrdersToCancel.size());

        //------------------------------------------------------------------------------------------------------------

        exchangeOrdersExisted = new ArrayList<>();
        exchangeOrder = new ExchangeOrder(null,null,900.0,10.03, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        exchangeOrder = new ExchangeOrder(null,null,800.0,10.01, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        exchangeOrder = new ExchangeOrder(null,null,700.0,10.00, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        exchangeOrdersToCreate = new ArrayList<>();
        exchangeOrdersToCancel = new ArrayList<>();
        logLines = new ArrayList<>();

        orderHandler.assignNewCancelExchangeOrders(exchangeOrdersToCreate, exchangeOrdersToCancel,exchangeOrdersExisted, allocations, logLines);

        assertEquals(2,exchangeOrdersToCreate.size());
        assertEquals(2,exchangeOrdersToCancel.size());

        //------------------------------------------------------------------------------------------------------------

        exchangeOrdersExisted = new ArrayList<>();
        exchangeOrder = new ExchangeOrder(null,null,900.0,10.03, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        exchangeOrder = new ExchangeOrder(null,null,800.0,10.02, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        exchangeOrder = new ExchangeOrder(null,null,700.0,10.00, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrdersExisted.add(exchangeOrder);

        exchangeOrdersToCreate = new ArrayList<>();
        exchangeOrdersToCancel = new ArrayList<>();
        logLines = new ArrayList<>();

        orderHandler.assignNewCancelExchangeOrders(exchangeOrdersToCreate, exchangeOrdersToCancel,exchangeOrdersExisted, allocations, logLines);

        assertEquals(1,exchangeOrdersToCreate.size());
        assertEquals(1,exchangeOrdersToCancel.size());
    }
}

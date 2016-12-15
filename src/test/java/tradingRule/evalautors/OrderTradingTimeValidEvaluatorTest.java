package tradingRule.evalautors;

import com.csc108.model.cache.ReferenceDataManager;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.evaluators.ClientOrderTradingTimeValidEvaluator;
import com.csc108.tradingRule.evaluators.NumOfOrdersPerAccountEvaluator;
import com.csc108.utility.AlertManager;
import junit.framework.TestCase;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix42.NewOrderSingle;
import utility.TestFixMsgHelper;

/**
 * Created by zhangbaoshan on 2016/12/13.
 */
public class OrderTradingTimeValidEvaluatorTest extends TestCase {
    public void testEvaluate() throws Exception {
        AlertManager.getInstance().init();
        ReferenceDataManager.getInstance().init();

        IEvaluator evaluator = new ClientOrderTradingTimeValidEvaluator();
        assertEquals("ClientOrderTradingTimeValidEvaluator",evaluator.getEvaluatorName());

        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-12:30:00","20161212-15:00:00");
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,null);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);
        orderHandler.initialize();

        boolean result= evaluator.evaluate(orderHandler,"True");
        assertEquals(false,result);

        newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-13:00:00","20161212-15:00:00");
        clientOrder = new ClientOrder(newOrderSingle,null);
        orderHandler = new OrderHandler(clientOrder,null);
        orderHandler.initialize();

        result= evaluator.evaluate(orderHandler,"True");
        assertEquals(true,result);
    }
}

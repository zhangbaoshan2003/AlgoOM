package tradingRule.evalautors;

import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.providers.EvaluatorProvider;
import junit.framework.TestCase;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class AccountEvaluatorTest extends TestCase {
    public void testEvaluate()throws Exception{
        EvaluatorProvider.initialize();
        IEvaluator evaluator = EvaluatorProvider.getEvaluators().get("AccountIDEvaluator");

        ClientOrder clientOrder  =new ClientOrder();
        clientOrder.setAccountId("123456");
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);
        boolean result= evaluator.evaluate(orderHandler,"123");
        assertEquals(false,result);

        result= evaluator.evaluate(orderHandler,"123456");
        assertEquals(true,result);
    }
}

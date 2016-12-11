package tradingRule.evalautors;

import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.model.fixModel.order.OrderPool;
import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.evaluators.NumOfOrdersPerAccountEvaluator;
import junit.framework.TestCase;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class NumOfAccountEvaluatorTest extends TestCase {

    public void testEvaulate() throws Exception {
        IEvaluator evaluator = new NumOfOrdersPerAccountEvaluator();
        assertEquals("NumOfOrdersPerAccountEvaluator",evaluator.getEvaluatorName());

        ClientOrder clientOrder = new ClientOrder();
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);

        clientOrder.setAccountId("test");
        try{
            evaluator.evaluate(orderHandler,"123456");
        }catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            assertEquals(ex.getMessage(),"Invalid criteria set for NumOfOrdersPerAccountEvaluator @ 123456");
        }

        try{
            clientOrder.setAccountId("test");
            evaluator.evaluate(orderHandler,"act:154d");
        }catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            assertEquals(ex.getMessage(), "Invalid criteria set for NumOfOrdersPerAccountEvaluator @ act:154d");
        }

        try{
            clientOrder.setAccountId("test");
            evaluator.evaluate(orderHandler,"test:154");
        }catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            assertEquals(ex.getMessage(), "Invalid criteria set for NumOfOrdersPerAccountEvaluator @ test:154");
        }

        evaluator.evaluate(orderHandler,"Acct_check_025:154");

        try{
            evaluator.evaluate(orderHandler,"258795 154");
        }catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            assertEquals(ex.getMessage(), "Invalid criteria set for NumOfOrdersPerAccountEvaluator @ 258795 154");
        }

        clientOrder.setAccountId("258795");
        boolean result= evaluator.evaluate(orderHandler,"258795:3");
        assertEquals(false, result);

        clientOrder.setAccountId("Acct_TPZC_025");
        result= evaluator.evaluate(orderHandler,"Acct_TPZC_025:3");
        assertEquals(false, result);

        for (int i=0;i<3;i++){
            ClientOrder newOrder = new ClientOrder();
            newOrder.setAccountId("258795");
            OrderPool.getClientOrderMap().put(newOrder.getClientOrderId(),newOrder);
        }
        result= evaluator.evaluate(orderHandler,"258795:3");
        assertEquals(false, result);

        result= evaluator.evaluate(orderHandler,"258795:2");
        assertEquals(true, result);
    }
}

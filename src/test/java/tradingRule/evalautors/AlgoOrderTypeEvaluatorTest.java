package tradingRule.evalautors;

import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.evaluators.AlgoOrderTypeEvaluator;
import junit.framework.TestCase;

import static org.mockito.Mockito.*;

/**
 * Created by zhangbaoshan on 2016/11/2.
 */
public class AlgoOrderTypeEvaluatorTest extends TestCase {

    public void testEvaluate() throws Exception {
        OrderHandler orderHandler = mock(OrderHandler.class);
        when(orderHandler.isPeggingOrder()).thenReturn(true);
        IEvaluator algoTypeEvaluator = new AlgoOrderTypeEvaluator();

        boolean expecException=false;
        try{
            algoTypeEvaluator = new AlgoOrderTypeEvaluator();
            algoTypeEvaluator.evaluate(orderHandler,"aaa");
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            expecException=true;
        }

        assertEquals(true, expecException);

        boolean result= algoTypeEvaluator.evaluate(orderHandler,"Pegging");
        assertEquals(true,result);

        result= algoTypeEvaluator.evaluate(orderHandler,"Normal");
        assertEquals(false,result);

        when(orderHandler.isPeggingOrder()).thenReturn(false);
        result= algoTypeEvaluator.evaluate(orderHandler,"Pegging");
        assertEquals(false,result);
    }
}

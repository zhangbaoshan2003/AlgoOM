package drools;

import com.csc108.configuration.GlobalConfig;
import com.csc108.drools.DroolsType;
import com.csc108.drools.DroolsUtility;
import com.csc108.drools.OrderMessage;
import junit.framework.TestCase;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;

import java.util.Date;

/**
 * Created by zhangbaoshan on 2016/5/23.
 */
public class DroolsTest extends TestCase  {
    public void testNewSingleOrderRule() throws Exception {
        System.out.println("Initializing drools rules ...");
        DroolsUtility.init(GlobalConfig.getNewSingleOrderRuleFiles(), DroolsType.NEW_SINGLE_ORDER_REQUEST);

        NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID("Order1"), new HandlInst('1'), new Symbol("IBM"),
                new Side(Side.BUY), new TransactTime(),  new OrdType(OrdType.MARKET));
        newOrderSingle.set(new Text("test text"));
        EffectiveTime effectiveTime = new EffectiveTime();
        effectiveTime.setValue(new Date());
        newOrderSingle.setField(effectiveTime);
        newOrderSingle.setString(6297,"VWAP");

        assertEquals(true, newOrderSingle.isSetField(6297));

        OrderMessage message = new OrderMessage(newOrderSingle);
        DroolsUtility.processMessage(message, DroolsType.NEW_SINGLE_ORDER_REQUEST);

        assertEquals(false,newOrderSingle.isSetField(6297));

    }
}

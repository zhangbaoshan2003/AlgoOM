package monitor;


import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.monitor.command.ClientOrderCommand;
import com.csc108.monitor.command.CommandFactory;
import com.csc108.tradingRule.core.IRule;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import com.csc108.utility.DateTimeUtil;
import utility.TestCaseBase;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhangbaoshan on 2016/5/25.
 */
public class CommandParserTest extends TestCaseBase {

    public static void testOptionArgumentValue() throws Exception {
        try {
            String[] args = new String[]{"fixModel","engine","list_all_sessions"};
            //CommandBase cmd = CommandFactory.getInstance().runCommand(args);
            //String response  = cmd.run(args);

            String response  = CommandFactory.getInstance().runCommand(args);

            System.out.println(response);
            assertFalse(response == "N/A");
            assertNotNull(response);

            args = new String[]{"client","order","cancel","-o","123456"};
            response  = CommandFactory.getInstance().runCommand(args);
            System.out.println(response);
            assertFalse(response == "N/A");
            assertNotNull(response);

        }catch (Exception ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    public void test_list_all_orders_command() throws Exception {

        int quantity = 10000;
        String symbol = "IBM";
        CountDownLatch stepGateWay = new CountDownLatch(4);
        ClientOrder clientOrder= clientApplication.sendNewSingleOrderThenFilledIt(symbol, quantity, 10.5d, stepGateWay);

        Thread.sleep(1000);

        ClientOrderCommand cmd = new ClientOrderCommand();
        String info= cmd.list_all_orders(new String[]{});

        System.out.println(info);

        assertNotNull(info);
        assertTrue(info.length()>10);

    }

    public void test_startime_endtime_parse(){
        String time = "20160701-01:16:59";
        LocalDateTime localDateTime= DateTimeUtil.getDateTime5(time).plusHours(8);
        System.out.println(localDateTime);
    }

    public void test_apply_cancel_command_parse(){
        String[] args = new String[]{"client","order","apply_cancel","-o","123456","-f","true"};
        String response  = CommandFactory.getInstance().runCommand(args);
        System.out.println(response);
    }

    public void test_trading_rule_list_command_parse(){
        String[] args = new String[]{"trading","rule","list"};
        String result = CommandFactory.getInstance().runCommand(args);
        System.out.println(result);
    }

    public void test_trading_rule_display_command(){
        String[] args = new String[]{"trading","rule","display","-i","0"};
        String result = CommandFactory.getInstance().runCommand(args);
        System.out.println(result);
    }

    public void test_trading_rule_ue_command(){
        IRule rule= TradingRuleProvider.getInstance().getTradingRules().get(0);
        assertEquals("MaximumOrdersRule",rule.getRuleName());
        assertEquals("Acct_PB_01:10",rule.getEvaluatorCriterias().get(1).get("NumOfOrdersPerAccountEvaluator"));

        String[] args = new String[]{"trading","rule","ue","-i","0","-n","NumOfOrdersPerAccountEvaluator","-c","Acct_PB_01:20"};

        String result = CommandFactory.getInstance().runCommand(args);
        assertEquals("update successfully!",result);

        assertEquals("Acct_PB_01:20",rule.getEvaluatorCriterias().get(1).get("NumOfOrdersPerAccountEvaluator"));

    }
}

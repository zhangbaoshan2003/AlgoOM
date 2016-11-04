package tradingRule;

import com.csc108.tradingRule.providers.TradingRuleProvider;
import junit.framework.TestCase;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class TradingRuleProviderTest extends TestCase {

    public void testInitializeTradingRuleProvider() throws Exception {
        TradingRuleProvider.getInstance().initialize("NormalTradingRule.xml");
    }
}

package tradingRule;

import com.csc108.decision.IDecisionConfig;
import com.csc108.decision.configuration.FinisherDecisionConfig;
import com.csc108.decision.configuration.PegConfiguration;
import com.csc108.decision.pegging.PegRiskFactor;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import junit.framework.TestCase;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class TradingRuleProviderTest extends TestCase {

    public void testInitializeTradingRuleProvider() throws Exception {
        TradingRuleProvider.getInstance().initialize("PeggingTradingRule.xml");

        assertEquals(2, TradingRuleProvider.getInstance().getConfigCache().keySet().size());
        FinisherDecisionConfig finisherDecision =(FinisherDecisionConfig) TradingRuleProvider.getInstance().getConfigCache().get("com.csc108.decision.configuration.FinisherDecisionConfig")
                .get(0);

        assertNotNull(finisherDecision);
        assertEquals(true, finisherDecision.isEnabled());
        assertEquals(60, finisherDecision.getFinisherTimeOffInSeconds());

        PegConfiguration pegConfiguration = (PegConfiguration)TradingRuleProvider.getInstance().getConfigCache().get("com.csc108.decision.configuration.PegConfiguration")
                .get(0);
        assertNotNull(pegConfiguration);
        assertEquals(PegRiskFactor.Low,pegConfiguration.getRiskFactor());
        assertEquals(3,pegConfiguration.getLadderLevel());
        assertEquals(1000,pegConfiguration.getDisplaySize());
    }
}

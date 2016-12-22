package configuration;

import com.csc108.decision.IDecisionConfig;
import com.csc108.decision.configuration.PauseResumeDecisionConfig;
import com.csc108.decision.configuration.PegConfiguration;
import com.csc108.model.cache.DecisionConfigCache;
import junit.framework.TestCase;

/**
 * Created by zhangbaoshan on 2016/12/20.
 */
public class DecisionConfigCacheTest extends TestCase {

    public void testCacheBehavior(){
        DecisionConfigCache<IDecisionConfig> decisionConfigDecisionConfigCache = new DecisionConfigCache<>();
        PegConfiguration pegConfiguration = new PegConfiguration();
        PauseResumeDecisionConfig pauseResumeDecisionConfig = new PauseResumeDecisionConfig();

        decisionConfigDecisionConfigCache.put(pegConfiguration.getClass().getName(),pegConfiguration);
        decisionConfigDecisionConfigCache.put(pauseResumeDecisionConfig.getClass().getName(),pauseResumeDecisionConfig);

        IDecisionConfig decisionConfig  = decisionConfigDecisionConfigCache.get("com.csc108.decision.configuration.PegConfiguration");
        assertEquals(decisionConfig.hashCode(),pegConfiguration.hashCode());

        decisionConfig  = decisionConfigDecisionConfigCache.get("com.csc108.decision.configuration.PauseResumeDecisionConfig");
        assertEquals(decisionConfig.hashCode(),pauseResumeDecisionConfig.hashCode());
    }
}

package database;

import com.csc108.model.cache.IssueTypeDataManager;
import junit.framework.TestCase;
import utility.TestCaseBase;

/**
 * Created by zhangbaoshan on 2016/8/30.
 */
public class IssueTypeDataManagerTest extends TestCase {

    public void testIsstyeDataManagerTest() throws Exception {
        IssueTypeDataManager.getInstance().initialize();
        assertTrue(IssueTypeDataManager.getInstance().IssueTypeHashMap().size()>1);
    }
}

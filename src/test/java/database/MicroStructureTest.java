package database;

import com.csc108.model.cache.MicroStructureDataManager;
import com.csc108.model.market.MicroStructure;
import junit.framework.TestCase;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class MicroStructureTest extends TestCase {
    public void testMicroStructure() throws Exception {
        MicroStructureDataManager.getInstance().initialize();
        assertTrue(MicroStructureDataManager.getInstance().getMicroStructureHashMap().size() > 1);

        MicroStructure ms = MicroStructureDataManager.getInstance().getMicroStructure("600000.sh");
        assertEquals("600000.sh", ms.getSymbol());

        ms = MicroStructureDataManager.getInstance().getMicroStructure("dummy.sh");
        assertEquals("default.sh",ms.getSymbol());

    }
}

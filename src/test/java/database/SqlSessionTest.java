package database;

import com.csc108.database.SqlSession;
import com.csc108.log.LogFactory;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import quickfix.SystemTimeSource;
import utility.TestUtility;

import java.io.File;
import java.sql.ResultSet;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class SqlSessionTest extends TestCase {

    @Override
    protected void setUp() {
        try{
            FileUtils.cleanDirectory(new File("Syslog"));
        }catch (Exception ex){
            System.out.println(ex);
        }
    }

    public void testSqlSession(){
        try{
            SqlSession session = new SqlSession();
            ResultSet ds = session.dataSetQuery("SELECT TOP(100)* FROM dbo.DailyFacts");
            session.stop();
        }catch (Exception ex){
            LogFactory.error("test failed",ex);
        }

        boolean noException = TestUtility.checkFileEmpty("Syslog/Error.log");
        assertEquals("Exception happened!", true, noException);
    }
}

package log;

import com.csc108.log.LogFactory;
import com.csc108.log.LogThread;
import com.csc108.log.NewFilePerOrderLogHandler;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/8/3.
 */
public class NewFilePerOrderLoggingTest extends TestCase {

    public void  testLogFile() throws Exception {

        System.out.printf("%n %s  %n%n","che");
        String tag526 = "000023500041_000001_102457";
        String tag1 = tag526.substring(0,tag526.indexOf('_'));
        System.out.println(tag1);

//        LogThread logThread = new LogThread();
//        logThread.start();
//
//        for (int i=0;i<10;i++){
//            Collection<String> lines = new ArrayList<>();
//            lines.add("This");
//            lines.add("check");
//            lines.add("end!EWERW!@#!$%#$");
//            NewFilePerOrderLogHandler handler = new NewFilePerOrderLogHandler("order"+i,lines);
//            logThread.enqueueTask(handler);
//        }
//
//        TimeUnit.SECONDS.sleep(1);
//        logThread.interrupt();

    }

    public void testLogFactor() throws Exception {
        for (int i=0;i<10;i++){
            ArrayList<String> lines = new ArrayList<>();
            lines.add("This");
            lines.add("check");
            lines.add("end!EWERW!@#!$%#$");
            LogFactory.logOrder("order@"+i,lines);
        }

        TimeUnit.SECONDS.sleep(1);


    }
}

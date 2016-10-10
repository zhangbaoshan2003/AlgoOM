package configuration;

import com.csc108.configuration.GlobalConfig;
import junit.framework.TestCase;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/5/7.
 */
public class GlobalConfigTest  extends TestCase {

    public void testConfigurationInitialize() throws Exception {
        assertEquals(true, GlobalConfig.ifUseDisruptorBlockingQueue());
        assertEquals(true,GlobalConfig.ifApplyActiveMq());
    }

    public void testLocalTimeCompare() throws Exception {
        LocalTime l1 = LocalTime.now();
        TimeUnit.MICROSECONDS.sleep(1);
        LocalTime l2 = LocalTime.now();
        if(l1.compareTo(l2)>0){
            System.out.printf("l2 %s greater then l1 %s",l2,l1);
        }
        if(l1.compareTo(l2)<0){
            System.out.printf("l2 %s less then l1 %s", l2, l1);
        }
        if(l1.compareTo(l2)==0){
            System.out.printf("l2 %s equal to l1 %s", l2, l1);
        }
    }
}

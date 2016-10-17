package infrastructure;

import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.market.OrderBook;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import utility.TestUtility;

import java.io.File;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by LEGEN on 2016/7/15.
 */
public class OrderbookDataManagerTest extends TestCase {
    private final String Level2DataTopicName = "quotahq";
    private final String MQ_RETROACTIVE = "?consumer.retroactive=true";
    private static final ScheduledExecutorService exc=Executors.newScheduledThreadPool(1);
    private static final ScheduledExecutorService outputService=Executors.newScheduledThreadPool(1);

    private Random random = new Random(7);

    protected void clearLog(){
        try{
            FileUtils.deleteDirectory(new File("Syslog"));
            FileUtils.deleteDirectory(new File("log"));
            FileUtils.deleteDirectory(new File("store"));
        }catch (Exception ex){
            System.out.println(ex);
        }
    }

    @Override
    protected void setUp() throws Exception {
        clearLog();
        OrderbookDataManager manager = OrderbookDataManager.getInstance();
        manager.initialize();
    }

    @Override
    protected void tearDown() throws Exception {
        OrderbookDataManager.getInstance().stop();
    }

    private void startPublishOrderbook(){
        exc.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                OrderbookDataManager.getInstance().publishData("000300", random.nextDouble(), random.nextDouble());
                OrderbookDataManager.getInstance().publishData("100000", random.nextDouble(), random.nextDouble());
                OrderbookDataManager.getInstance().publishData("000002", random.nextDouble(), random.nextDouble());
            }
        }, 1000, 200, TimeUnit.MICROSECONDS);
    }

    private void stopPublishData(){
        exc.shutdown();
    }

    public void testOrderbookDataManagerSubscribeSecurity() throws Exception {
        OrderbookDataManager.getInstance().subscribeOrderBook("000001.sh", false,null);
        OrderbookDataManager.getInstance().subscribeOrderBook("000300.sh", false, null);
        //OrderbookDataManager.getInstance().publishData("600000", random.nextDouble(), random.nextDouble());

        outputService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(OrderbookDataManager.getInstance().toString());
            }
        }, 1000, 1000, TimeUnit.MICROSECONDS);

        startPublishOrderbook();
        Thread.sleep(3 * 1000);
        stopPublishData();

        boolean noException = TestUtility.checkFileEmpty("Syslog/Error.log");
        assertEquals("Exception happened!", true, noException);
    }

    public void testSubscribeSZSecurityWorks() throws Exception {
        OrderbookDataManager.getInstance().subscribeOrderBook("300367.sz", false,null);
        outputService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(OrderbookDataManager.getInstance().toString());
            }
        }, 1000, 1000, TimeUnit.MICROSECONDS);
        Thread.sleep(3 * 1000);
    }

    public void testPublishDataToMqWorks() throws Exception {
        String symbol = "600000";
        double preClose=11.01;
        double lastPx = 11.02;
        double openPx = 11.03;
        double closPx = 11.04;
        double ap=10.10;
        double bp = 10.01;
        OrderbookDataManager.getInstance().subscribeOrderBook(symbol + ".sh", false, null);

        OrderbookDataManager.getInstance().publish_SH_SecurityData(symbol, preClose, lastPx, openPx,
                closPx, ap, bp);

        TimeUnit.SECONDS.sleep(1);

        OrderBook ob = OrderbookDataManager.getInstance().getLatestOrderBook(symbol+".sh");
        assertNotNull(ob);

        System.out.println(OrderbookDataManager.getInstance().toString());
        System.out.println(ob);

        boolean noException = TestUtility.checkFileEmpty("Syslog/Error.log");
        assertEquals("Exception happened!", true, noException);
    }
}

package utility.disruptor;

import com.csc108.disruptor.PerformanceCounter;
import com.lmax.disruptor.EventHandler;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class LongEventHandler implements EventHandler<LongEvent> {
    public final static long NOW = System.currentTimeMillis();
    private final PerformanceCounter counter = new PerformanceCounter();

    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) throws Exception {
        // System.out.println("Event: " + event);
        long l = event.get();
        PerformanceCounter.increase();

//        if (l % 10000 == 0) {
//            long num = l  / ((System.currentTimeMillis() - NOW)*1000);
//            //System.out.println(String.format("1st handler -- %s per sec", num));
//            event.setName_("name + " + num);
//            System.out.println(event.getName_());
//            //System.out.println(String.format("1st handler -- the %s th event", l));
//        }
    }

}

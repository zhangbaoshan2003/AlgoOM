package disruptor;

import com.csc108.disruptor.PerformanceCounter;
import com.csc108.disruptor.concurrent.DisruptorController;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import junit.framework.TestCase;
import utility.disruptor.LongEvent;
import utility.disruptor.LongEventFactory;
import utility.disruptor.LongEventHandler;
import utility.disruptor.LongEventProducer;

import java.nio.ByteBuffer;
import java.util.concurrent.*;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class MultipleDisruptorTest extends TestCase {

    private LongEventProducer buildProducer (EventFactory factory,int bufferSize,Executor executor){
        Disruptor<LongEvent> disruptor1  = new Disruptor<LongEvent>(factory, bufferSize,
                Executors.newSingleThreadExecutor(),
                ProducerType.MULTI,new SleepingWaitStrategy());

        // Connect the handler
        disruptor1.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor1.start();

        RingBuffer<LongEvent> ringBuffer = disruptor1.getRingBuffer();
        LongEventProducer producer1 = new LongEventProducer(ringBuffer);
        return  producer1;
    }

    public void testPerformanceWithMultipleDisruptor() throws Exception {
        CountDownLatch gateWay = new CountDownLatch(1);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executorSvr = Executors.newFixedThreadPool(4);

        Executor executor = Executors.newCachedThreadPool();

        // The factory for the event
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 8192;

        // Construct the Disruptor
        LongEventProducer producer1 = buildProducer(factory,bufferSize,executor);
        LongEventProducer producer2 = buildProducer(factory,bufferSize,executor);
        LongEventProducer producer3 = buildProducer(factory,bufferSize,executor);
        LongEventProducer producer4 = buildProducer(factory,bufferSize,executor);

        long begin = System.currentTimeMillis();

        executorSvr.execute(new Runnable() {
            @Override
            public void run() {
                ByteBuffer bb = ByteBuffer.allocate(8);
                for (long l = 0; true; l++) {
                    bb.putLong(0, l);
                    producer1.onData(bb);
                    if((System.currentTimeMillis()-begin)>1000){
                        break;
                    }
                }
            }
        });

        executorSvr.execute(new Runnable() {
            @Override
            public void run() {
                ByteBuffer bb = ByteBuffer.allocate(8);
                for (long l = 0; true; l++) {
                    bb.putLong(0, l);
                    producer2.onData(bb);
                    if((System.currentTimeMillis()-begin)>1000){
                        break;
                    }
                }
            }
        });

        executorSvr.execute(new Runnable() {
            @Override
            public void run() {
                ByteBuffer bb = ByteBuffer.allocate(8);
                for (long l = 0; true; l++) {
                    bb.putLong(0, l);
                    producer3.onData(bb);
                    if((System.currentTimeMillis()-begin)>1000){
                        break;
                    }
                }
            }
        });

        executorSvr.execute(new Runnable() {
            @Override
            public void run() {
                ByteBuffer bb = ByteBuffer.allocate(8);
                for (long l = 0; true; l++) {
                    bb.putLong(0, l);
                    producer4.onData(bb);
                    if((System.currentTimeMillis()-begin)>1000){
                        break;
                    }
                }
            }
        });

        TimeUnit.SECONDS.sleep(5);
        System.out.println(PerformanceCounter.getCounter());

    }
}

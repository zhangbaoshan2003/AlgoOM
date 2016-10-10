package disruptor;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.PerformanceCounter;
import com.csc108.disruptor.concurrent.EventDispatcher;
import com.csc108.disruptor.event.EventType;
import com.csc108.disruptor.event.OmEvent;
import com.lmax.disruptor.BlockingWaitStrategy;
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
public class PerformanceTest extends TestCase {

    public void testCounter() throws Exception {
        CountDownLatch gateWay = new CountDownLatch(1);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executorSvr = Executors.newFixedThreadPool(2);

        Executor executor = Executors.newCachedThreadPool();

        // The factory for the event
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 8192;

        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(factory, bufferSize, executor);

        disruptor = new Disruptor<LongEvent>(factory, bufferSize,
                Executors.newSingleThreadExecutor(),
                ProducerType.MULTI,new SleepingWaitStrategy());

        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducer producer1 = new LongEventProducer(ringBuffer);
        LongEventProducer producer2 = new LongEventProducer(ringBuffer);
        LongEventProducer producer3 = new LongEventProducer(ringBuffer);
        LongEventProducer producer4 = new LongEventProducer(ringBuffer);


        long begin = System.currentTimeMillis();
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            bb.putLong(0, l);
            producer1.onData(bb);
            if((System.currentTimeMillis()-begin)>1000){
                break;
            }
        }

        TimeUnit.SECONDS.sleep(1);
        System.out.println(PerformanceCounter.getCounter());



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

package utility.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    public LongEvent newInstance() {
        return new LongEvent();
    }
}

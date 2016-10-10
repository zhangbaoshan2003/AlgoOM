package utility.disruptor;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class LongEvent {
    private long value;
    private String name_;

    public void set(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    public String getName_() {
        return name_;
    }

    public void setName_(String name_) {
        this.name_ = name_;
    }
}

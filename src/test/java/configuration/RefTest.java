package configuration;

import com.csc108.decision.configuration.Ref;
import junit.framework.TestCase;

/**
 * Created by zhangbaoshan on 2016/5/9.
 */
public class RefTest extends TestCase {
    private void changeValueFun(Ref<Double> value){
        value.setValue(value.getValue()-5.7);
    }
    public void testRefWorks(){
        Ref<Double> originalQty = new Ref<>(10.12d);
        changeValueFun(originalQty);
        assertEquals(originalQty.getValue(), 10.12 - 5.7);

        String a=new String("ABC");
        String b="ABC";
        System.out.println(a.hashCode());
        System.out.println(b.hashCode());
    }
}

package mock;

import junit.framework.TestCase;

import java.util.LinkedList;
import static org.mockito.Mockito.*;

/**
 * Created by zhangbaoshan on 2016/11/2.
 */
public class MockTest extends TestCase {

    public void testMock(){
        LinkedList mockedList = mock(LinkedList.class);
        //using mock
        mockedList.add("once");

        mockedList.add("twice");
        mockedList.add("twice");

        mockedList.add("three times");
        mockedList.add("three times");
        mockedList.add("three times");

        //following two verifications work exactly the same - times(1) is used by default
        verify(mockedList).add("once");
        verify(mockedList, times(1)).add("once");

        //exact number of invocations verification
        verify(mockedList, times(2)).add("twice");
        verify(mockedList, times(3)).add("three times");

        //verification using never(). never() is an alias to times(0)
        verify(mockedList, never()).add("never happened");
    }

    public void testInteraction(){
        LinkedList mockedList = mock(LinkedList.class);
        //using mock
        mockedList.add("once");
        mockedList.add("twice");
        verify(mockedList).add("once");
        verify(mockedList).add("twice");
        verifyNoMoreInteractions(mockedList);
    }
}

package model;

import com.csc108.model.market.Quote;
import junit.framework.TestCase;
import org.junit.Test;

public class QuoteTest extends TestCase {
	Quote quote = new Quote(0.2, 100.23);

	@Test
	public void testToString() {
		System.out.println(Boolean.toString(Double.compare(0.0,0)==0));
		assertEquals("0.2@100.23", quote.toString());
	}
}

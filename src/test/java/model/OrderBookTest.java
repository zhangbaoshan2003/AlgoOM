package model;

import com.csc108.model.data.OrderSide;
import com.csc108.model.data.Security;
import com.csc108.model.data.SecurityType;
import com.csc108.model.market.OrderBook;
import com.csc108.model.market.Quote;
import com.csc108.utility.Util;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.junit.Test;
import utility.TestUtility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderBookTest {

	@Test
	public void testGetAsk() {
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));

		OrderBook book = OrderBook.of();
		book.setAsk(ask);
		book.setBid(bid);

		Quote expected = new Quote(10.0, 1000);
		assertEquals(expected, book.getAsk(0));

		expected = new Quote(10.1, 2000);
		assertEquals(expected, book.getAsk(1));

		expected = new Quote(Util.DefaultSellPrice, Util.DefaultSellQty);
		assertEquals(expected, book.getAsk(10));
		assertEquals(expected, book.getAsk(-1));
	}

	@Test
	public void testGetAskPrice() {
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));

		OrderBook book = OrderBook.of();
		book.setAsk(ask);
		book.setBid(bid);

		assertEquals(10.0, book.getAskPrice(0), TestUtility.Delta);
		assertEquals(10.1, book.getAskPrice(1), TestUtility.Delta);

		assertEquals(Util.DefaultSellPrice, book.getAskPrice(10), TestUtility.Delta);
		assertEquals(Util.DefaultSellPrice, book.getAskPrice(-1), TestUtility.Delta);
	}

	@Test
	public void testGetAskQty() {
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));

		OrderBook book = OrderBook.of();
		book.setAsk(ask);
		book.setBid(bid);

		assertEquals(1000, book.getAskQty(0), TestUtility.Delta);
		assertEquals(2000, book.getAskQty(1), TestUtility.Delta);

		assertEquals(Util.DefaultSellQty, book.getAskQty(10), TestUtility.Delta);
		assertEquals(Util.DefaultSellQty, book.getAskQty(-1), TestUtility.Delta);
	}

	@Test
	public void testGetBid() {
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));

		OrderBook book = OrderBook.of();
		book.setAsk(ask);
		book.setBid(bid);

		Quote expected = new Quote(9.9, 4000);
		assertEquals(expected, book.getBid(0));

		expected = new Quote(9.8, 5000);
		assertEquals(expected, book.getBid(1));

		expected = new Quote(Util.DefaultBuyPrice, Util.DefaultBuyQty);
		assertEquals(expected, book.getBid(10));
		assertEquals(expected, book.getBid(-1));
	}

	@Test
	public void testGetBidPrice() {
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));

		OrderBook book = OrderBook.of();
		book.setAsk(ask);
		book.setBid(bid);

		assertEquals(9.9, book.getBidPrice(0), TestUtility.Delta);
		assertEquals(9.8, book.getBidPrice(1), TestUtility.Delta);

		assertEquals(Double.MIN_VALUE, book.getBidPrice(10), TestUtility.Delta);
		assertEquals(Double.MIN_VALUE, book.getBidPrice(-1), TestUtility.Delta);
	}

	@Test
	public void testGetBidQty() {
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));

		OrderBook book = OrderBook.of();
		book.setAsk(ask);
		book.setBid(bid);

		assertEquals(4000, book.getBidQty(0), TestUtility.Delta);
		assertEquals(5000, book.getBidQty(1), TestUtility.Delta);

		assertEquals(Util.DefaultBuyQty, book.getBidQty(10), TestUtility.Delta);
		assertEquals(Util.DefaultBuyQty, book.getBidQty(-1), TestUtility.Delta);
	}

	@Test
	public void testGetFarPrice() {
		OrderBook book = OrderBook.of();

		assertEquals(Util.DefaultBuyPrice, book.getFarPrice(OrderSide.Buy), TestUtility.Delta);
		assertEquals(Util.DefaultSellPrice, book.getFarPrice(OrderSide.Sell), TestUtility.Delta);

		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));
		book.setAsk(ask);

		assertEquals(10.0, book.getFarPrice(OrderSide.Buy), TestUtility.Delta);
		assertEquals(10.1, book.getFarPrice(OrderSide.Buy, 1), TestUtility.Delta);
		assertEquals(10.2, book.getFarPrice(OrderSide.Buy, 2), TestUtility.Delta);
		assertEquals(Util.DefaultBuyPrice, book.getFarPrice(OrderSide.Buy, 3), TestUtility.Delta);
		assertEquals(Util.DefaultSellPrice, book.getFarPrice(OrderSide.Sell), TestUtility.Delta);

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));
		book.setBid(bid);

		assertEquals(9.9, book.getFarPrice(OrderSide.Sell), TestUtility.Delta);
		assertEquals(9.8, book.getFarPrice(OrderSide.Sell, 1), TestUtility.Delta);
	}

	@Test
	public void testGetFarSize() {
		OrderBook book = OrderBook.of();

		assertEquals(Util.DefaultBuyQty, book.getFarSize(OrderSide.Buy), TestUtility.Delta);
		assertEquals(Util.DefaultBuyQty, book.getFarSize(OrderSide.Buy, 1), TestUtility.Delta);
		assertEquals(Util.DefaultSellQty, book.getFarSize(OrderSide.Sell), TestUtility.Delta);
		assertEquals(Util.DefaultSellQty, book.getFarSize(OrderSide.Sell, 1), TestUtility.Delta);

		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));
		book.setAsk(ask);

		assertEquals(1000, book.getFarSize(OrderSide.Buy), TestUtility.Delta);
		assertEquals(2000, book.getFarSize(OrderSide.Buy, 1), TestUtility.Delta);
		assertEquals(3000, book.getFarSize(OrderSide.Buy, 2), TestUtility.Delta);
		assertEquals(Util.DefaultBuyQty, book.getFarSize(OrderSide.Buy, 3), TestUtility.Delta);
		assertEquals(Util.DefaultSellQty, book.getFarSize(OrderSide.Sell), TestUtility.Delta);

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));
		book.setBid(bid);

		assertEquals(4000, book.getFarSize(OrderSide.Sell), TestUtility.Delta);
		assertEquals(6000, book.getFarSize(OrderSide.Sell, 2), TestUtility.Delta);
	}
	
	@Test
	public void testGetNearPrice() {
		OrderBook book = OrderBook.of();

		assertEquals(Util.DefaultBuyPrice, book.getNearPrice(OrderSide.Buy), TestUtility.Delta);
		assertEquals(Util.DefaultBuyPrice, book.getNearPrice(OrderSide.Buy, 1), TestUtility.Delta);
		assertEquals(Util.DefaultSellPrice, book.getNearPrice(OrderSide.Sell), TestUtility.Delta);
		assertEquals(Util.DefaultSellPrice, book.getNearPrice(OrderSide.Sell, 1), TestUtility.Delta);

		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));
		book.setAsk(ask);

		assertEquals(10.0, book.getNearPrice(OrderSide.Sell), TestUtility.Delta);
		assertEquals(10.1, book.getNearPrice(OrderSide.Sell, 1), TestUtility.Delta);
		assertEquals(Util.DefaultSellPrice, book.getNearPrice(OrderSide.Sell, 3), TestUtility.Delta);
		assertEquals(Util.DefaultBuyPrice, book.getNearPrice(OrderSide.Buy), TestUtility.Delta);

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));
		book.setBid(bid);

		assertEquals(9.9, book.getNearPrice(OrderSide.Buy), TestUtility.Delta);
		assertEquals(9.7, book.getNearPrice(OrderSide.Buy, 2), TestUtility.Delta);
	}
	
	@Test
	public void testGetNearSize() {
		OrderBook book = OrderBook.of();

		assertEquals(Util.DefaultBuyQty, book.getNearSize(OrderSide.Buy), TestUtility.Delta);
		assertEquals(Util.DefaultBuyQty, book.getNearSize(OrderSide.Buy, 1), TestUtility.Delta);
		assertEquals(Util.DefaultSellQty, book.getNearSize(OrderSide.Sell), TestUtility.Delta);
		assertEquals(Util.DefaultSellQty, book.getNearSize(OrderSide.Sell, 1), TestUtility.Delta);

		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));
		book.setAsk(ask);

		assertEquals(1000, book.getNearSize(OrderSide.Sell), TestUtility.Delta);
		assertEquals(3000, book.getNearSize(OrderSide.Sell, 2), TestUtility.Delta);
		assertEquals(Util.DefaultSellQty, book.getNearSize(OrderSide.Sell, 4), TestUtility.Delta);
		assertEquals(Util.DefaultBuyQty, book.getNearSize(OrderSide.Buy), TestUtility.Delta);

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));
		book.setBid(bid);

		assertEquals(4000, book.getNearSize(OrderSide.Buy), TestUtility.Delta);
		assertEquals(6000, book.getNearSize(OrderSide.Buy, 2), TestUtility.Delta);
	}
	
	@Test
	public void testToEvaluationString() {
		List<Quote> ask = new ArrayList<Quote>();
        ask.add(new Quote(10.0, 1000));
        ask.add(new Quote(10.1, 2000));
        ask.add(new Quote(10.2, 3000));

        List<Quote> bid = new ArrayList<Quote>();
        bid.add(new Quote(9.9, 4000));
        bid.add(new Quote(9.8, 5000));
        bid.add(new Quote(9.7, 6000));

        OrderBook book = OrderBook.of(ask, bid);
        assertEquals("bid:9.9@4000.0,9.8@5000.0,9.7@6000.0|ask:10.2@3000.0,10.1@2000.0,10.0@1000.0", book.toEvaluationString());    
	}

	@Test
	public void testClone() {
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));

		OrderBook book = OrderBook.of(ask, bid);
		OrderBook book2 = (OrderBook) book.clone();
		assertEquals(false, book == book2);
		assertEquals(book, book2);
	}
	
	@Test
	public void testComparator() {
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));

		OrderBook ob1 = OrderBook.of(ask, bid);
		ob1.setDateTime(LocalDateTime.now().withYear(2013));
		
		OrderBook ob2 = (OrderBook)ob1.clone();
		ob2.setDateTime(LocalDateTime.now().withYear(2012));
		
		List<OrderBook> books = Lists.newArrayList(ob1, ob2);
		Collections.sort(books, OrderBook.TimeComparator);

		assertEquals(ob1, books.get(1));
		assertEquals(ob2, books.get(0));

		OrderBook ob3 = (OrderBook)ob1.clone();
		OrderBook ob4 = (OrderBook)ob1.clone();
		ob4.setDateTime(ob4.getDateTime().withYear(2014));
		OrderBook ob5 = (OrderBook)ob4.clone();
		books.add(ob3);
		books.add(ob4);
		books.add(ob5);
		books.add((OrderBook)ob1.clone());
		books.add((OrderBook)ob4.clone());

		Collections.sort(books, OrderBook.TimeComparator);
		int real = Collections.binarySearch(books, (OrderBook)ob1.clone(), OrderBook.TimeComparator2);
		if(real < 0) {
			real = 0 - (real + 1); //adjust for from
		}		
		assertEquals(1, real);
		
		
		real = Collections.binarySearch(books, (OrderBook)ob4.clone(), OrderBook.TimeComparator3);
		if(real < 0) {
			real = 0 - (real + 1) - 1;
			//real = real == books.size() ? books.size() - 1 : real;
		}
		assertEquals(6, real); //adjust for to
		
		OrderBook ob8 = (OrderBook)ob1.clone();
		ob8.setDateTime(ob8.getDateTime().withYear(2222));
		real = Collections.binarySearch(books, ob8, OrderBook.TimeComparator3);
		if(real < 0) {
			real = 0 - (real + 1) - 1;
			//real = real == books.size() ? books.size() - 1 : real;
		}
		
		assertEquals(6, real); //adjust for to
	}
	
	@Test
	public void testEquals() {
		Security sec1 = Security.of("000001.sz", SecurityType.Stock);
		List<Quote> ask = new ArrayList<Quote>();
		ask.add(new Quote(10.0, 1000));
		ask.add(new Quote(10.1, 2000));
		ask.add(new Quote(10.2, 3000));

		List<Quote> bid = new ArrayList<Quote>();
		bid.add(new Quote(9.9, 4000));
		bid.add(new Quote(9.8, 5000));
		bid.add(new Quote(9.7, 6000));
		
		OrderBook ob1 = OrderBook.of(LocalDateTime.now());
		ob1.setSecurity(sec1);
		ob1.setAsk(ask);
		ob1.setBid(bid);
		
		OrderBook ob2 = (OrderBook)ob1.clone();
		assertEquals(true, ob1.equals(ob2));
		assertTrue(ob1.equals(ob2));

	}

	@Test
	public void testCirularList(){
		CircularFifoQueue<String> orderBooks = new CircularFifoQueue<>(3);
		orderBooks.add("A");
		assertEquals(orderBooks.size(), 1);

		orderBooks.add("B");
		orderBooks.add("C");
		assertEquals(orderBooks.size(), 3);

		orderBooks.add("D");
		assertEquals(orderBooks.size(), 3);
		assertEquals("D", orderBooks.get(2));
		assertEquals("C", orderBooks.get(1));
		assertEquals("B", orderBooks.get(0));

		orderBooks.add("F");
		assertEquals(orderBooks.size(), 3);
		assertEquals("F", orderBooks.get(2));
		assertEquals("D", orderBooks.get(1));
		assertEquals("C", orderBooks.get(0));
	}
}

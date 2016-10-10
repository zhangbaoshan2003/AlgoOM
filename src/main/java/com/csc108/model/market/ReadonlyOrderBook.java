package com.csc108.model.market;

import com.csc108.model.data.OrderSide;
import com.csc108.model.data.Security;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class ReadonlyOrderBook extends OrderBook implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4329230122806461899L;
	private OrderBook book;
	
	public ReadonlyOrderBook(OrderBook book_) {
		book = book_;	
		book.setAsk(ImmutableList.copyOf(book.getAsk()));
		book.setBid(ImmutableList.copyOf(book.getBid()));
	}

	@Override
	public List<Quote> getAsk() {
		return book.getAsk();
	}

	@Override
	public void setAsk(List<Quote> ask) {
		throw new UnsupportedOperationException();
	} 

	@Override
	public List<Quote> getBid() {
		return book.getBid();
	}

	@Override
	public void setBid(List<Quote> bid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getLastPrice() {
		return book.getLastPrice();
	}

	@Override
	public void setLastPrice(double lastPrice) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Security getSecurity() {
		return book.getSecurity();
	}

	@Override
	public void setSecurity(Security security_) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getTurnover() {
		return book.getTurnover();
	}

	@Override
	public void setTurnover(double turnover) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getDoneVolume() {
		return book.getDoneVolume();
	}

	@Override
	public void setDoneVolume(double doneVolume) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getPreClose() {
		return book.getPreClose();
	}

	@Override
	public void setPreClose(double preClose) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getOpenPrice() {
		return book.getOpenPrice();
	}

	@Override
	public void setOpenPrice(double openPrice) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getClosePrice() {
		return book.getClosePrice();
	}

	@Override
	public void setClosePrice(double closePrice) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getHighestPrice() {
		return book.getHighestPrice();
	}

	@Override
	public void setHighestPrice(double highestPrice) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getLowestPrice() {
		return book.getLowestPrice();
	}

	@Override
	public void setLowestPrice(double lowestPrice) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public LocalDateTime getDateTime() {
		return book.getDateTime();
	}
	
	@Override
	public void setDateTime(LocalDateTime timeNow_) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isSingleSided() {
		return book.isSingleSided();
	}

	@Override
	public double getFarPrice(OrderSide orderSide_, int level_) {
		return book.getFarPrice(orderSide_, level_);
	}

	@Override
	public double getFarPrice(OrderSide orderSide_) {
		return book.getFarPrice(orderSide_);
	}

	@Override
	public double getNearPrice(OrderSide orderSide_, int level_) {
		return book.getNearPrice(orderSide_, level_);
	}

	@Override
	public double getNearPrice(OrderSide orderSide_) {
		return book.getNearPrice(orderSide_);
	}

	@Override
	public double getFarSize(OrderSide orderSide_, int level_) {
		return book.getFarSize(orderSide_, level_);
	}

	@Override
	public double getFarSize(OrderSide orderSide_) {
		return book.getFarSize(orderSide_);
	}

	@Override
	public double getNearSize(OrderSide orderSide_, int level_) {
		return book.getNearPrice(orderSide_, level_);
	}

	@Override
	public double getNearSize(OrderSide orderSide_) {
		return book.getNearSize(orderSide_);
	}

	@Override
	public Quote getAsk(int level_) {
		return book.getAsk(level_);
	}

	@Override
	public Quote getBid(int level_) {
		return book.getBid(level_);
	}

	@Override
	public double getAskPrice(int level_) {
		return book.getAskPrice(level_);
	}

	@Override
	public double getAskQty(int level_) {
		return book.getAskQty(level_);
	}

	@Override
	public double getBidPrice(int level_) {
		return book.getBidPrice(level_);
	}

	@Override
	public double getBidQty(int level_) {
		return book.getBidQty(level_);
	}

	@Override
	public String toEvaluationString() {
		return book.toEvaluationString();
	}

	@Override
	public double getSpread() {
		return book.getSpread();
	}

	@Override
	public boolean equals(Object obj_) {
		if (obj_ == null) {
			return false;
		}
		if (obj_ == this) {
			return true;
		}
		if (obj_.getClass() != getClass()) {
			return false;
		}
		ReadonlyOrderBook ob = (ReadonlyOrderBook) obj_;
		return book.equals(ob.book);
	}

	@Override
	public int hashCode() {
		return book.hashCode();
	}

	@Override
	public Object clone() {
		OrderBook bookNew = (OrderBook) book.clone();
		return new ReadonlyOrderBook(bookNew);
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();
		s.writeObject(book);
	}

	private void readObject(java.io.ObjectInputStream s_) throws java.io.IOException, ClassNotFoundException {
		s_.defaultReadObject();
		book = (OrderBook) s_.readObject();
	}
}

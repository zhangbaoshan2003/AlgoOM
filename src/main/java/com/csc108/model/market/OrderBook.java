package com.csc108.model.market;

import com.csc108.model.ITimeable;
import com.csc108.model.data.OrderSide;
import com.csc108.model.data.Security;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import quickfix.field.Side;
import com.csc108.utility.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.csc108.utility.*;



public class OrderBook implements Cloneable, Serializable, ITimeable,Comparable<OrderBook> {
	private static final long serialVersionUID = 5661552489004666908L;
	private List<Quote> bid;
	private double lastPrice;
	private Security security;
	private double turnover;
	private double doneVolume;
	private double preClose;
	private double openPrice;
	private double closePrice;
	private double highestPrice;
	private double lowestPrice;
	private LocalDateTime dateTime;

	public List<Quote> getBid() {
		return bid;
	}

	public void setBid(List<Quote> bid) {
		this.bid = bid;
	}

	public double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public double getTurnover() {
		return turnover;
	}

	public void setTurnover(double turnover) {
		this.turnover = turnover;
	}

	public double getDoneVolume() {
		return doneVolume;
	}

	public void setDoneVolume(double doneVolume) {
		this.doneVolume = doneVolume;
	}

	public double getPreClose() {
		return preClose;
	}

	public void setPreClose(double preClose) {
		this.preClose = preClose;
	}

	public double getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}

	public double getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(double closePrice) {
		this.closePrice = closePrice;
	}

	public double getHighestPrice() {
		return highestPrice;
	}

	public void setHighestPrice(double highestPrice) {
		this.highestPrice = highestPrice;
	}

	public double getLowestPrice() {
		return lowestPrice;
	}

	public void setLowestPrice(double lowestPrice) {
		this.lowestPrice = lowestPrice;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	@Override
	public long getQty() {
		return 0;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	
	public OrderBook() {
		this(null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.MIN);
	}

	private List<Quote> ask;
	public List<Quote> getAsk(){
		return ask;
	}
	public void setAsk(List<Quote> quotes){
		ask=quotes;
	}
	
	private OrderBook(Security sec_, List<Quote> ask_, List<Quote> bid_, LocalDateTime dt_) {
		this(ask_, bid_, -1, sec_, -1, -1, -1, -1, -1, -1, -1, dt_);
	}
	
	private OrderBook(List<Quote> ask_, List<Quote> bid_, double lastPrice_, Security sec_, double turnover_, double doneVolume_,
					  double preClose_, double openPrice_, double closePrice_, double hp_, double lp_, LocalDateTime bookTime_) {
		this.ask = ask_;
		this.bid = bid_;
		this.lastPrice = lastPrice_;
		this.security = sec_;
		this.turnover = turnover_;
		this.doneVolume = doneVolume_;
		this.preClose = preClose_;
		this.openPrice = openPrice_;
		this.closePrice = closePrice_;
		this.highestPrice = hp_;
		this.lowestPrice = lp_;
		this.dateTime = bookTime_;
	}
	
	public static OrderBook of(List<Quote>ask_, List<Quote>bid_, double lastPrice_, Security sec_, double turnover_, double doneVolume_,
			double preClose_, double op_, double cp_, double hp_, double lp_, LocalDateTime bookTime_) {
		return new OrderBook(ask_, bid_, lastPrice_, sec_, turnover_, doneVolume_, preClose_, op_, cp_, hp_, lp_, bookTime_);
	}
	
	public static OrderBook of() {
		return of(LocalDateTime.MIN);
	}
	
	public static OrderBook of(LocalDateTime dt_) {
		return of(new ArrayList<Quote>(), new ArrayList<Quote>(), dt_);
	}
	
	public static OrderBook of(List<Quote>ask_, List<Quote>bid_, LocalDateTime dt_) {
		return of(null, ask_, bid_, dt_);
	}
	
	public static OrderBook of(Security sec_, List<Quote>ask_, List<Quote>bid_, LocalDateTime dt_) {
		return new OrderBook(sec_, ask_, bid_, dt_);
	}
	
	public static OrderBook of(List<Quote>ask_, List<Quote>bid_) {
		return of(ask_, bid_, LocalDateTime.MIN);
	}
	
	public boolean isSingleSided() {
		return ask.size() + bid.size() > 0 && (ask.size() == 0 || bid.size() == 0);
	}

	public double getFarPrice(OrderSide orderSide_, int level_) {
		List<Quote> quotes = orderSide_ == OrderSide.Buy ? ask : bid;

		if (quotes.size() <= level_ || !Util.isValidPrice(quotes.get(level_).getPrice()))
			return Util.defaultPrice(orderSide_);
		else
			return quotes.get(level_).getPrice();
	}

	public double getFarPrice(OrderSide orderSide_) {
		return getFarPrice(orderSide_, 0);
	}

	public double getNearPrice(OrderSide orderSide_, int level_) {
		List<Quote> quotes = orderSide_ == OrderSide.Buy ? bid : ask;

		if (quotes.size() <= level_ || !Util.isValidPrice(quotes.get(level_).getPrice()))
			return Util.defaultPrice(orderSide_);
		else
			return quotes.get(level_).getPrice();
	}

	public double getNearPrice(OrderSide orderSide_) {
		return getNearPrice(orderSide_, 0);
	}

	public double getFarSize(OrderSide orderSide_, int level_) {
		List<Quote> quotes = orderSide_ == OrderSide.Buy ? ask : bid;

		if (quotes.size() <= level_ || !Util.isValidPrice(quotes.get(level_).getQty()))
			return Util.defaultQty(orderSide_);
		else
			return quotes.get(level_).getQty();
	}

	public double getFarSize(OrderSide orderSide_) {
		return getFarSize(orderSide_, 0);
	}

	public double getNearSize(OrderSide orderSide_, int level_) {
		List<Quote> quotes = orderSide_ == OrderSide.Buy ? bid : ask;

		if (quotes.size() <= level_ || !Util.isValidPrice(quotes.get(level_).getQty()))
			return Util.defaultQty(orderSide_);
		else
			return quotes.get(level_).getQty();
	}

	public double getNearSize(OrderSide orderSide_) {
		return getNearSize(orderSide_, 0);
	}

	public Quote getAsk(int level_) {
		if (level_ >= ask.size() || level_ < 0) {
			return new Quote(Util.DefaultSellPrice, Util.DefaultSellQty);
		} else {
			return (Quote) ask.get(level_).clone();
		}
	}

	public Quote getBid(int level_) {
		if (level_ >= bid.size() || level_ < 0) {
			return new Quote(Util.DefaultBuyPrice, Util.DefaultBuyQty);
		} else {

			return (Quote) bid.get(level_).clone();
		}
	}

	public double getAskPrice(int level_) {
		return getAsk(level_).getPrice();
	}

	public double getAskQty(int level_) {
		return getAsk(level_).getQty();
	}

	public double getBidPrice(int level_) {
		return getBid(level_).getPrice();
	}

	public double getBidQty(int level_) {
		return getBid(level_).getQty();
	}

	public String toEvaluationString() {
		StringBuilder strBuilder = new StringBuilder();
		// bid
		strBuilder.append("bid:");
		for (Quote q : bid) {
			strBuilder.append(String.format("%s,", q.toString()));
		}

		// remove the trailing ','
		if (strBuilder.charAt(strBuilder.length() - 1) == ',') {
			strBuilder.setCharAt(strBuilder.length() - 1, '|');
		}

		// ask
		strBuilder.append("ask:");
		for (int i = ask.size() - 1; i >= 0; i--) {
			strBuilder.append(String.format("%s,", ask.get(i).toString()));
		}

		if (strBuilder.charAt(strBuilder.length() - 1) == ',') {
			strBuilder.deleteCharAt(strBuilder.length() - 1);
		}

		return strBuilder.toString();
	}

	public double getSpread() {
		return getAskPrice(0) - getBidPrice(0);
	}
	
	public boolean isValid() {
		return security != null && ask.size() + bid.size() > 0; 
	}

	@Override
	public boolean equals(Object obj_) {
		return EqualsBuilder.reflectionEquals(this, obj_);
	}

	@Override
	public int hashCode() {
		throw  new NotImplementedException();
	}

	@Override
	public Object clone() {
		return Util.deepCopy(this);
	}

	public boolean princeLevelEquals(OrderBook ob, int level){
		for (int i=0;i<level;i++){
			if(this.getAsk(i).getPrice()!=ob.getAsk(i).getPrice() || this.getBid(i).getPrice()!=ob.getBid(i).getPrice()){
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		FormattedTable table = new FormattedTable();
		List<Object> row = new ArrayList<Object>();
		row.add("as");
		row.add("ap");
		row.add("|");
		row.add("bp");
		row.add("bs");
		table.AddRow(row);

		// ask side
		int count = ask.size()>3?3:ask.size();
		count=3;
		for (int i = count - 1; i >= 0; --i) {
			row = new ArrayList<Object>();
			row.add(ask.get(i).getQty());
			row.add(ask.get(i).getPrice());
			row.add("|");
			row.add(" ");
			row.add(" ");
			table.AddRow(row);
		}

		// bid side
		count = bid.size()>3?3:bid.size();

		for (int i = 0; i < count; ++i) {
			row = new ArrayList<Object>();
			row.add(" ");
			row.add(" ");
			row.add("|");
			row.add(bid.get(i).getPrice());
			row.add(bid.get(i).getQty());
			table.AddRow(row);
		}

		return String.format("%s@%s las px:%f close px:%f \r\n %s",
				getSecurity().getSymbol(),getDateTime(),
				getLastPrice(),
				getPreClose(),
				table.toString());
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		PutField putFields = s.putFields();
		putFields.put("ask", ask);
		putFields.put("bid", bid);
		putFields.put("lastPrice", lastPrice);
		putFields.put("security", security);
		putFields.put("turnover", turnover);
		putFields.put("doneVolume", doneVolume);
		putFields.put("preClose", preClose);
		putFields.put("openPrice", openPrice);
		putFields.put("closePrice", closePrice);
		putFields.put("highestPrice", highestPrice);
		putFields.put("lowestPrice", lowestPrice);
		putFields.put("dateTime", dateTime);
		s.writeFields();
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s_) throws java.io.IOException, ClassNotFoundException {
		GetField getFields = s_.readFields();

		ask = (List<Quote>) getFields.get("ask", null);
		bid = (List<Quote>) getFields.get("bid", null);
		lastPrice = getFields.get("lastPrice", MathUtil.MinusOne);
		security = (Security) getFields.get("security", null);
		turnover = getFields.get("turnover", MathUtil.MinusOne);
		doneVolume = getFields.get("doneVolume", MathUtil.MinusOne);
		preClose = getFields.get("preClose", MathUtil.MinusOne);
		openPrice = getFields.get("openPrice", MathUtil.MinusOne);
		closePrice = getFields.get("closePrice", MathUtil.MinusOne);
		highestPrice = getFields.get("highestPrice", MathUtil.MinusOne);
		lowestPrice = getFields.get("lowestPrice", MathUtil.MinusOne);
		dateTime = (LocalDateTime) getFields.get("dateTime", null);
	}

	public ReadonlyOrderBook asReadOnly() {
		return new ReadonlyOrderBook((OrderBook) clone());
	}

	@Override
	public int compareTo(OrderBook o) {
		return this.getDateTime().compareTo(o.getDateTime());
	}
}

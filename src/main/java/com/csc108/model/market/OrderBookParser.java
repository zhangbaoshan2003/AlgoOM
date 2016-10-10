package com.csc108.model.market;

import com.csc108.model.data.Security;
import com.csc108.model.data.SecurityType;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderBookParser {		
	public static OrderBook parse(String msg_) {
		SAXBuilder builder = new SAXBuilder();
		
		Document doc = null;
		try {			
			doc = builder.build(new StringReader(msg_));
		} catch (JDOMException | IOException e_) {
			throw new IllegalArgumentException("Unparsable Data msg_", e_);
		}

		Element record = doc.getRootElement().getChild("body").getChild("record");
		String stkcode = record.getAttributeValue("stkcode");
		String marketid = record.getAttributeValue("marketid");
		String symbol = stkcode + "." + marketid;

		double preClose = parseDouble(record.getAttributeValue("preclose"));
		LocalDateTime bookTime = LocalDateTime.now();
		double lastPrice = parseDouble(record.getAttributeValue("lastprice"));
		double turnover = parseDouble(record.getAttributeValue("turnover"));
		double doneVolume = parseDouble(record.getAttributeValue("donevolume"));
		double openPrice = parseDouble(record.getAttributeValue("openprice"));
		double closePrice = parseDouble(record.getAttributeValue("closeprice"));
		double highestPrice = parseDouble(record.getAttributeValue("highestprice"));
		double lowestPrice = parseDouble(record.getAttributeValue("lowestprice"));

		List<Quote> ask = new ArrayList<Quote>();
		List<Quote> bid = new ArrayList<Quote>();
		for (int i = 1; i <= 10; i++) {
			bid.add(new Quote(parseDouble(record.getAttributeValue("bidprice" + i)), parseDouble(record.getAttributeValue("bidvolume" + i))));
			ask.add(new Quote(parseDouble(record.getAttributeValue("askprice" + i)), parseDouble(record.getAttributeValue("askvolume" + i))));
		}
		
		/*if (stkcode.startsWith("8") || stkcode.startsWith("9") || stkcode.startsWith("H")) {
			System.out.println(msg_);
		}*/

		OrderBook book = OrderBook.of(ask, bid, lastPrice, Security.of(symbol, SecurityType.Stock), turnover, doneVolume, preClose, openPrice, closePrice, highestPrice, lowestPrice, bookTime);
		return book;
	}
	
	/*The default */
	public static double parseDouble(String v_) {
		return StringUtils.isEmpty(v_) || v_.equals("-") || v_.equals("-.---")? -1 : Double.parseDouble(v_);
	}
}

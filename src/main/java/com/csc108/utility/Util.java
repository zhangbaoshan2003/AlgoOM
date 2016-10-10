package com.csc108.utility;

import com.csc108.model.data.OrderSide;
import com.csc108.model.market.OrderBook;
import com.csc108.model.market.Transaction;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.SerializationUtils;

import javax.jms.MapMessage;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Util {	
	public final static double DefaultSellPrice = Double.MAX_VALUE;
	public final static double DefaultBuyPrice = Double.MIN_VALUE;
	public final static double DefaultBuyQty = BigDecimal.valueOf(-1).intValue(); // 0
																					// is
																					// a
																					// valid
																					// qty
																					// such
																					// as
																					// in
																					// an
																					// uplimit
																					// or
																					// downlimit	
    public final static double DefaultSellQty = BigDecimal.valueOf(-1).intValue();    
    
    
    
	public static boolean isValidPrice(double price_) {
		return price_ >= 0 && price_ != Util.DefaultBuyPrice && price_ != Util.DefaultSellPrice;
	}

	public static double defaultPrice(OrderSide side_) {
		return side_ == OrderSide.Buy ? DefaultBuyPrice : DefaultSellPrice;
	}

	public static double defaultQty(OrderSide side_) {
		return side_ == OrderSide.Buy ? DefaultBuyQty : DefaultSellQty;
	}
	
	public static <T extends Serializable> T deepCopy(T o_) {
		return SerializationUtils.clone(o_);
	}
	
	public static double roundPriceNearestTick(double price_, double tickSize_) {
        return Math.floor(price_ / tickSize_ + 0.5) * tickSize_;
    }

	public static Path getRootDir() {
		Path path = null;
		try {
			path = Paths.get(com.csc108.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return path;
	}
	
	public static Path getDir(@SuppressWarnings("rawtypes") Class clazz_) {
		Path path = null;
		try {
			//String path = StringUtils.replace(text, searchString, replacement)
			path = Paths.get(clazz_.getResource(".").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return path;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> mapMsg2Map(MapMessage mmsg_) {
		Map<String, String> msg = new HashMap<>();		
		try {
			Iterator<String> itr = Iterators.forEnumeration(mmsg_.getMapNames());
			while(itr.hasNext()) {
				String k = itr.next();
				msg.put(k, (String)mmsg_.getString(k));
			}
		} catch (Exception e_) {
			throw new RuntimeException(e_);
		}
		return msg;
	}

	//TODO, replaced with a cache and allow 1 minute space
	public static boolean isTradable(LocalDateTime time_) {
		return true;		
	}
	
	public static double roundQtyDown(double qty_, double base_) {
		return Math.floor(qty_ / base_) * base_;
	}
	
	public static double roundQtyUp(double qty_, double base_) {
		return Math.ceil(qty_ / base_) * base_;
	}
	
	public static boolean isValidTransaction(Transaction t_) {
		//return t_.getPrice() != 0; //for sz it is the cancelled trade confirmation
		return true;
	}
	
	public static boolean isValidOrderBook(OrderBook b_) {
		return true;
	}
	
	public static double clamp(double v_, double min_, double max_) {
		return Math.max(min_, Math.min(v_, max_));
	}
	
	public static double roundQtyNear(double v_, double base_) {
		return Math.floor(v_ / base_ + 0.5) * base_;
	}	
	
	public static boolean isValidSymbol(String mdSymbol_) {
		return mdSymbol_.matches("^[^89a-zA-Z].*") && mdSymbol_.length() != 8;
	}
}

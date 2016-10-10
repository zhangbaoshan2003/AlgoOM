package com.csc108.model.market;

import com.csc108.exceptions.UnknownValueException;
import com.csc108.model.IEvaluationData;
import com.csc108.model.ITimeable;
import com.csc108.model.data.Security;
import com.csc108.utility.MathUtil;
import com.csc108.utility.Util;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaction implements IEvaluationData, Serializable, Cloneable, ITimeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8759352743389620363L;
	@Getter
	@Setter
	private double price;


	@Getter
	@Setter
	private double qty;	
	@Setter
	private LocalDateTime dateTime;
	@Getter
	@Setter
	private Security security;
	
	@Override
	public boolean equals(Object obj_) {
		return EqualsBuilder.reflectionEquals(this, obj_);
	}

	@Override
	public int hashCode() {
		return this.security.hashCode()+this.dateTime.hashCode();
	}

	@Override
	public Object clone() {
		return Util.deepCopy(this);
	}
	
	private Transaction(double px_, double vol_, LocalDateTime dt_, Security sec_) {
		price = px_;
		qty = vol_;
		dateTime = dt_;
		security = sec_;
	}	
	
	public static Transaction of(LocalDateTime time_) {
		return of(-1, -1, time_, null);
	}
	
	public static Transaction of(double px_, double vol_, LocalDateTime dt_, Security sec_) {
		return new Transaction(px_, vol_, dt_, sec_);
	}
	
	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		PutField putFields = s.putFields();
		putFields.put("price", price);
		putFields.put("qty", qty);
		putFields.put("datetime", dateTime);
		putFields.put("security", security);		
		s.writeFields();
	}

	private void readObject(java.io.ObjectInputStream s_) throws java.io.IOException, ClassNotFoundException {
		GetField getFields = s_.readFields();

		price = getFields.get("price", MathUtil.MinusOne);
		qty = getFields.get("qty", MathUtil.MinusOne);
		dateTime = (LocalDateTime)getFields.get("datetime", null);
		security = (Security)getFields.get("security", null);
	}
	
	public static enum TransactionSide implements Serializable {
		Buy(1), Sell(2), Neutral(3);
	    
	    @Getter
	    private int value;

	    private TransactionSide(int value_) {
	    	value = value_;
	    }
	    
	    public static TransactionSide of(int value_) {
	    	if(value_ == 1 || value_ == 2 || value_ == 3)
	    		return value_ == 1 ? Buy : (value_ == 2 ? Sell : Neutral);
	    	else 
				throw new UnknownValueException();
	    }
	    
	    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
			PutField putFields = s.putFields();
			putFields.put("value", value);			
			s.writeFields();
		}
		
		private void readObject(java.io.ObjectInputStream s_) throws java.io.IOException, ClassNotFoundException {
			GetField getFields = s_.readFields();
			value = getFields.get("value", -1);			
		}
	}

	@Override
	public LocalDateTime getDateTime() {
		return dateTime;
	}

	@Override
	public long getQty() {
		return 0;
	}

	public boolean isValid() {
		return security != null && price != 0 && qty != 0;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[security=").append(security.getSymbol()).append(",")
			.append("price=").append(price).append(",").append("qty=").append(qty)
			.append("]");
		return sb.toString();
	}
}

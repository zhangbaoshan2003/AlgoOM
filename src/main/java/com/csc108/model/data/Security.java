package com.csc108.model.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;
import java.util.Objects;

public class Security implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2174356994537890017L;
	@Getter
	@Setter
	private SecurityType type;

	private String symbol;
	public void setSymbol(String symbol){
		this.symbol = symbol;
	}
	public String getSymbol(){
		return this.symbol;
	}

	public static Security of(String symbol_, SecurityType type_) {
		return new Security(symbol_, type_);
	}
	
	public Security(String symbol_, SecurityType type_) {		
		symbol = symbol_;
		type = type_;
	}

	private String _HSName;

	@Override
	public int hashCode() {
		return Objects.hash(type, symbol);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		Security rhs = (Security) obj;
		return new EqualsBuilder().append(type, rhs.type).append(symbol, rhs.symbol).isEquals();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "@[symbol=" + symbol + ", type=" + type.toString() + "]";  
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		PutField putFields = s.putFields();
		putFields.put("type", type);
		putFields.put("symbol", symbol);
		s.writeFields();
	}

	private void readObject(java.io.ObjectInputStream s_) throws java.io.IOException, ClassNotFoundException {
		GetField getFields = s_.readFields();
		type = (SecurityType) getFields.get("type", null);
		symbol = (String) getFields.get("symbol", null);
	}

	public String get_HSName() {
		return _HSName;
	}

	public void set_HSName(String _HSName) {
		this._HSName = _HSName;
	}
}

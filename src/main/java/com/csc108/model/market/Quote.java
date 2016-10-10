package com.csc108.model.market;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;

public class Quote implements Cloneable, Serializable {	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8022093555923998701L;

	private double price;
	public void setPrice(double price){
		this.price = price;
	}
	public double getPrice(){
		return this.price;
	}


	private double qty;
	public double getQty(){return this.qty;}
	public void setQty(double qty){this.qty = qty;}

	public Quote(double price_, double qty_) {
		price = price_;
		qty = qty_;
	}

	@Override
	public String toString() {
		return String.format("%s@%s", price, qty);
	}

	@Override
	public Object clone() {
		return new Quote(price, qty);
	}
	
	@Override
	public boolean equals(Object o_) {		
		//TODO performance enhance	
		return EqualsBuilder.reflectionEquals(this, o_);
	}

	private void writeObject(java.io.ObjectOutputStream s_) throws java.io.IOException {
		s_.defaultWriteObject();
		s_.writeDouble(price);
		s_.writeDouble(qty);
	}

	private void readObject(java.io.ObjectInputStream s_) throws java.io.IOException, ClassNotFoundException {
		s_.defaultReadObject();
		price = s_.readDouble();
		qty = s_.readDouble();
	}
}

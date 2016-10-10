package com.csc108.model.data;

import com.csc108.exceptions.UnknownValueException;
import lombok.Getter;

import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream.PutField;

public enum OrderSide { 
    Buy(1), Sell(2);
    
    @Getter
    private int value;

    private OrderSide(int value_) {
    	value = value_;
    }
    
    public static OrderSide of(int value_) {
    	if(value_ == 1 || value_ == 2)
    		return value_ == 1 ? Buy : Sell;
    	else {
			throw new UnknownValueException();
		}
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
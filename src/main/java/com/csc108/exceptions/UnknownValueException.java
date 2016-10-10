package com.csc108.exceptions;

public class UnknownValueException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownValueException() {
		super();
	}
	
	public UnknownValueException(String msg_) { 
		super(msg_);
	}
	
	public UnknownValueException(String msg_, Throwable cause_) {
		super(msg_, cause_);
	}
	
	public UnknownValueException(Throwable cause_) {
		super(cause_);
	}
}

package com.csc108.exceptions;

public class TimeOutException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TimeOutException() {
		super();
	}
	
	public TimeOutException(String msg_) { 
		super(msg_);
	}
	
	public TimeOutException(String msg_, Throwable cause_) {
		super(msg_, cause_);
	}
	
	public TimeOutException(Throwable cause_) {
		super(cause_);
	}
}

package com.csc108.exceptions;

public class CacheOperationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CacheOperationException() {
		super();
	}
	
	public CacheOperationException(String msg_) { 
		super(msg_);
	}
	
	public CacheOperationException(String msg_, Throwable cause_) {
		super(msg_, cause_);
	}
	
	public CacheOperationException(Throwable cause_) {
		super(cause_);
	}
}

package com.csc108.model.data;

import lombok.Getter;

public enum SecurityType {
	Stock(1), ETF(2);

	@Getter
	private int value;

	private SecurityType(int value_) {
		value = value_;
	}
}
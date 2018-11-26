package com.twincoders.twinpush.sdk.entities;

public enum PropertyType {

	STRING("string"), BOOLEAN("boolean"), INTEGER("integer"), FLOAT("float"), ENUM("enum");

	private String valueType;

	PropertyType(String valueType) {
		this.valueType = valueType;
	}

	public String getValueType() {
		return valueType;
	}
}

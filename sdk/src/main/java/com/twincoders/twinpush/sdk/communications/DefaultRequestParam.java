package com.twincoders.twinpush.sdk.communications;

import java.util.List;

public class DefaultRequestParam implements TwinRequestParam {
	
	String key = null;
	Object value = null;
	List<TwinRequestParam> innerParams = null;
	List<String> arrayValue = null;
	ParamType paramType;
	
	private DefaultRequestParam() {
		super();
	}
	
	DefaultRequestParam(String key, Object value) {
		super();
		this.key = key;
		this.value = value;
		this.paramType = ParamType.SIMPLE;
	}
	
	public static DefaultRequestParam simpleParam(String key, Object value) {
		return new DefaultRequestParam(key, value);
	}
	
	public static DefaultRequestParam complexParam(String key, List<TwinRequestParam> innerParams) {
		DefaultRequestParam param = new DefaultRequestParam();
		param.key = key;
		param.innerParams = innerParams;
		param.paramType = ParamType.COMPLEX;
		return param;
	}
	
	public static DefaultRequestParam arrayParam(String key, List<String> arrayValue) {
		DefaultRequestParam param = new DefaultRequestParam();
		param.key = key;
		param.arrayValue = arrayValue;
		param.paramType = ParamType.ARRAY;
		return param;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public Object getValue() {
		return value;
	}
	
	@Override
	public List<String> getArrayValue() {
		return arrayValue;
	}
	
	@Override
	public List<TwinRequestParam> getInnerParams() {
		return innerParams;
	}
	
	@Override
	public ParamType getParamType() {
		return paramType;
	}

}

package com.yellowpineapple.offers101.communications;

import java.util.List;

public class DefaultRequestParam implements RequestParam {
	
	String key = null;
	Object value = null;
	List<RequestParam> innerParams = null;
	List<Object> arrayValue = null;
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
	
	public static DefaultRequestParam complexParam(String key, List<RequestParam> innerParams) {
		DefaultRequestParam param = new DefaultRequestParam();
		param.key = key;
		param.innerParams = innerParams;
		param.paramType = ParamType.COMPLEX;
		return param;
	}
	
	public static DefaultRequestParam arrayParam(String key, List<Object> arrayValue) {
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
	public List<Object> getArrayValue() {
		return arrayValue;
	}
	
	@Override
	public List<RequestParam> getInnerParams() {
		return innerParams;
	}
	
	@Override
	public ParamType getParamType() {
		return paramType;
	}

}

package com.yellowpineapple.wakup.sdk.communications;

import java.util.List;

public interface RequestParam {
	
	public enum ParamType {
		SIMPLE,
		ARRAY,
		COMPLEX;
	}

	String getKey();
	Object getValue();
	ParamType getParamType();
	List<Object> getArrayValue();
	List<RequestParam> getInnerParams();
	
}

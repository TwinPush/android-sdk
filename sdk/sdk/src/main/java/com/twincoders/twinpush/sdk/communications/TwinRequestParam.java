package com.twincoders.twinpush.sdk.communications;

import java.util.List;

public interface TwinRequestParam {
	
	public enum ParamType {
		SIMPLE,
		ARRAY,
		COMPLEX;
	}

	String getKey();
	Object getValue();
	ParamType getParamType();
	List<String> getArrayValue();
	List<TwinRequestParam> getInnerParams();
	
}

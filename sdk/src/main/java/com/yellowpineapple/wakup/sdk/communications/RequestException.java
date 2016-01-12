package com.yellowpineapple.wakup.sdk.communications;

public class RequestException extends Exception {

    String code;
    String message;

    public RequestException(String code, String message) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public static RequestException missingFieldsException() {
        return new RequestException("", "Some of the required fields are missing in response");
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

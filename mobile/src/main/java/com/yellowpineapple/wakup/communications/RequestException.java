package com.yellowpineapple.wakup.communications;

import lombok.Getter;

public class RequestException extends Exception {

    @Getter
    String code;
    @Getter
    String message;

    public RequestException(String code, String message) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public static RequestException missingFieldsException() {
        return new RequestException("", "Some of the required fields are missing in response");
    }

}

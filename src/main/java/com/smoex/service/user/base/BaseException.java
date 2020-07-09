package com.smoex.service.user.base;

public class BaseException extends RuntimeException {
    private int code = -1;

    public BaseException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

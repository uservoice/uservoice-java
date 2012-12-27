package com.uservoice;

@SuppressWarnings("serial")
public class APIError extends RuntimeException {

    public APIError(String msg) {
        super(msg);
    }

}

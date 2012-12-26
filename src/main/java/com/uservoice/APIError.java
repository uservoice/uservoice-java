package com.uservoice;

@SuppressWarnings("serial")
public class APIError extends Exception {

    public APIError(String msg) {
        super(msg);
    }

}

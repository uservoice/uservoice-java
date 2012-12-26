package com.uservoice;

@SuppressWarnings("serial")
public class Unauthorized extends APIError {

    public Unauthorized(String msg) {
        super(msg);
    }

}

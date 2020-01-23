package com.chaochaogege.hotelapi.api;

public class ApiException extends Exception {
    private String cause;
    public ApiException(String cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return cause;
    }
}

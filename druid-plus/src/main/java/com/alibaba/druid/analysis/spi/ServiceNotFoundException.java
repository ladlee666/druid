package com.alibaba.druid.analysis.spi;

public class ServiceNotFoundException extends RuntimeException {

    private String message;

    public ServiceNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }
}

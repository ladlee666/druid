package com.alibaba.druid.analysis.seq;

public class SeqException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SeqException(String message) {
        super(message);
    }

    public SeqException(Throwable cause) {
        super(cause);
    }
}

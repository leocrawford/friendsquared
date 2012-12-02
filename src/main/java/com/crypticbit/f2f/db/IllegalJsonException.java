package com.crypticbit.f2f.db;

import com.fasterxml.jackson.core.JsonProcessingException;

public class IllegalJsonException extends Exception {

    public IllegalJsonException(String message, Throwable throwable) {
	super(message,throwable);
    }

}

package com.gurula.stockMate.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException (String message) {
        super(message);
    }
}

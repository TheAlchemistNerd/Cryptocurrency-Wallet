package com.cryptowallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate an invalid transaction operation,
 * such as insufficient funds, invalid signature, or duplicate transactions.
 * Maps to HTTP 400 Bad Request
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}

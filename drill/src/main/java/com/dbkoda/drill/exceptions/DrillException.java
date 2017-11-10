package com.dbkoda.drill.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public class DrillException extends RuntimeException{

    public DrillException() {
        super();
    }

    public DrillException(String message) {
        super(message);
    }

    public DrillException(String message, Throwable cause) {
        super(message, cause);
    }

    public DrillException(Throwable cause) {
        super(cause);
    }
}

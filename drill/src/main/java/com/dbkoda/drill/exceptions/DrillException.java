package com.dbkoda.drill.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DrillException extends RuntimeException {

    private int code;

    public DrillException(int code, String message) {
        super(message);
        this.code = code;
    }


    public DrillException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public DrillException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

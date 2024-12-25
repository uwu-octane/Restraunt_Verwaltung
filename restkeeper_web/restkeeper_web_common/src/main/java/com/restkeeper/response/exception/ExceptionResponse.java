package com.restkeeper.response.exception;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExceptionResponse implements Serializable {
    private String msg;
    public ExceptionResponse(String msg) {
        this.msg = msg;
    }
}

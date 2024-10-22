package com.tigo.workersupermarketott.core.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderCreateErrorResponse implements Serializable {
    private int errorCode;
    private String errorType;
    private String code;
    private String description;
}
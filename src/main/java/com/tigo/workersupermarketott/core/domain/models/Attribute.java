package com.tigo.workersupermarketott.core.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Attribute implements Serializable {
    private String id;
    private String externalId;
    private String orderDate;
    private String publicIdentifier;
    private String message;
}

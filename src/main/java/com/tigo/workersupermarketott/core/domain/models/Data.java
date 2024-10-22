package com.tigo.workersupermarketott.core.domain.models;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@lombok.Data
public class Data implements Serializable {
    private Attribute attributes;
}
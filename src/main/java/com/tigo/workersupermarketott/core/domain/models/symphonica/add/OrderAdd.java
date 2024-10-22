package com.tigo.workersupermarketott.core.domain.models.symphonica.add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderAdd implements Serializable {
	private String externalId;
    private int priority;
    private String description;
    private String category;
    private String region;
    private String source;
    private String orderType;
    private ArrayList<Object> notes;
    private ArrayList<OrderItem> orderItems;
    private ArrayList<Object> orderRelationships;
    private ArrayList<RelatedParty> relatedParty;
    private ArrayList<ExtraValue> extraValues;
}

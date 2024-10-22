package com.tigo.workersupermarketott.core.domain.models.symphonica.add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem implements Serializable {
	private int id;
	private String action;
	private ArrayList<Object> appointments;
	private ArrayList<Object> orderItemRelationships;
	private Service service;
}

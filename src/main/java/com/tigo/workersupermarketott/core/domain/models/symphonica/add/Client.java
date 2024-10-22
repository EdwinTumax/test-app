package com.tigo.workersupermarketott.core.domain.models.symphonica.add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client implements Serializable {
	private String msisdn;
	private String productId;
	private String productName;
	private String typeOperation;
	private String type;
	private String statusBilling;
	private String statusSymphonica;	
}

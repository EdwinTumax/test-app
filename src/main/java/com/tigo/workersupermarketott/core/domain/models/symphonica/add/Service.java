package com.tigo.workersupermarketott.core.domain.models.symphonica.add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Service implements Serializable {
	private ServiceSpecification serviceSpecification;
	private ArrayList<Characteristic> characteristics;
	private ArrayList<Object> places;
	private String publicIdentifier;
}
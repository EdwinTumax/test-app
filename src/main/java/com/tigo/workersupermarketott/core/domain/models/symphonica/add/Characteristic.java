package com.tigo.workersupermarketott.core.domain.models.symphonica.add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Characteristic implements Serializable {
	private String name;
	private String value;
    private ArrayList<CharacteristicRelationship> characteristicRelationships;
	public Characteristic(String name, String value){
		this.name = name;
		this.value = value;
	}

}

package com.tigo.workersupermarketott.core.domain.models.symphonica.add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceSpecification implements Serializable {
	 private String id;
	 private String source;
	 private String name;
	 private String code;
	 private String version;
}

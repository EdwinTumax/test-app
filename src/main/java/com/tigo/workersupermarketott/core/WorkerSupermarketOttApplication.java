package com.tigo.workersupermarketott.core;

import com.tigo.workersupermarketott.core.verticles.VerticleApiSupermarket;
import com.tigo.workersupermarketott.core.verticles.VerticleCreateOrder;
import com.tigo.workersupermarketott.core.verticles.VerticleStatusOrder;
import com.tigo.workersupermarketott.core.verticles.VerticleTokenSymphonica;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class WorkerSupermarketOttApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(WorkerSupermarketOttApplication.class);

	private Vertx vertx;

	@Autowired
	private VerticleApiSupermarket verticleApiSupermarket;
	@Autowired
	private VerticleTokenSymphonica verticleTokenSymphonica;
	@Autowired
	private VerticleCreateOrder verticleCreateOrder;
	@Autowired
	private VerticleStatusOrder verticleStatusOrder;
	@Autowired
	private Environment env;
	public static void main(String[] args) {
		SpringApplication.run(WorkerSupermarketOttApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

	}

	@PostConstruct
	public void deploymentVerticle(){
		this.vertx = Vertx.vertx();
		ConfigStoreOptions fileConfig = new ConfigStoreOptions()
				.setType("file")
				.setConfig(new JsonObject().put("path",env.getProperty("SPRING_PROFILES_ACTIVE").concat(".json")));
		ConfigStoreOptions sysPropStore = new ConfigStoreOptions().setType("sys");
		ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions().addStore(fileConfig).addStore(sysPropStore);
		ConfigRetriever configRetriever = ConfigRetriever.create(vertx,configRetrieverOptions);
		configRetriever.getConfig(config -> {
			if(config.succeeded()){
				this.vertx.deployVerticle(verticleApiSupermarket,new DeploymentOptions().setConfig(config.result()));
				this.vertx.deployVerticle(verticleTokenSymphonica,new DeploymentOptions().setConfig(config.result()));
				this.vertx.deployVerticle(verticleCreateOrder, new DeploymentOptions().setConfig(config.result()));
				this.vertx.deployVerticle(verticleStatusOrder, new DeploymentOptions().setConfig(config.result()));
				logger.info("Deployment Verticle");
			} else {
				logger.error("Error Deployment Verticle");
			}
		});

	}
}

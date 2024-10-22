package com.tigo.workersupermarketott.core.configs;


import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigBeanSupermarketOTT {

    @Bean
    public Gson Gson(){
        return new Gson();
    }
}

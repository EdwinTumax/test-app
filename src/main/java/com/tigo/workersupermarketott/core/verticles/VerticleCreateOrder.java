package com.tigo.workersupermarketott.core.verticles;

import com.google.gson.Gson;
import com.tigo.workersupermarketott.core.domain.models.OrderCreate;
import com.tigo.workersupermarketott.core.domain.models.symphonica.add.*;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class VerticleCreateOrder extends AbstractVerticle {
    private static final String EVENT_BUS_CREATE_ORDER = "EVENT_BUS_CREATE_ORDER";
    private static final String EVENT_BUS_STATUS_ORDER = "EVENT_BUS_STATUS_ORDER";
    private static final String CIRCUIT_BREAKER_ORDER = "CIRCUIT_BREAKER_ORDER";
    private CircuitBreaker circuitBreaker;

    private EventBus eventBus;
    private WebClient webClient;


    @Autowired
    private Gson gson;

    private static final Logger logger = LoggerFactory.getLogger(VerticleCreateOrder.class);


    @Override
    public void start() {
        this.eventBus = vertx.eventBus();
        this.webClient = WebClient.create(vertx);
        this.circuitBreaker = CircuitBreaker.create(this.CIRCUIT_BREAKER_ORDER,vertx,new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setTimeout(5000)
                        .setFallbackOnFailure(true)
                        .setResetTimeout(10000))
                .openHandler(circuit-> { this.logger.info("Circuit breaker opened"); })
                .closeHandler(circuit -> { this.logger.info("Circuit breaker closed");})
                .halfOpenHandler(circuit -> { this.logger.warn("Circuit breaker half-opened");});
        this.eventBus.consumer(this.EVENT_BUS_CREATE_ORDER, messageHandler -> {
            JsonObject jsonOrderCreate = new JsonObject(Json.encode(messageHandler.body()));
            OrderCreate orderCreate = gson.fromJson(jsonOrderCreate.toString(),OrderCreate.class);
            createOrder(this.webClient,orderCreate).onComplete(actionResult -> {
                //logger.info("respuesta web ".concat(actionResult.result().toString()));
                if(actionResult.succeeded()){
                    orderCreate.setOrderId(actionResult.result().getValue("orderId").toString());
                    this.eventBus.request(this.EVENT_BUS_STATUS_ORDER,JsonObject.mapFrom(orderCreate),statusHandler -> {
                        if(statusHandler.result() != null && statusHandler.result().body() != null) {
                            this.logger.info("valor body ".concat(statusHandler.result().body().toString()));
                            messageHandler.replyAndRequest(statusHandler.result().body());
                            /*String state = new JsonObject(Json.encode(statusHandler.result().body())).getValue("state").toString();
                            if(state.equalsIgnoreCase("COMPLETED")){
                                messageHandler.replyAndRequest(actionResult.result());
                                //statusHandler.result().replyAndRequest("COMPLETED");
                            } else if (state.equalsIgnoreCase("FAILED")){
                                messageHandler.replyAndRequest(actionResult.result());
                                //responseMessage.result().replyAndRequest("FAILED");
                            } else if (state.equalsIgnoreCase("PARTIAL")){
                                //responseMessage.result().replyAndRequest("CANCELLED");
                            } else if(state.equalsIgnoreCase("CANCELLED")){
                                //responseMessage.result().replyAndRequest("CANCELLED");
                            } else if(state.equalsIgnoreCase("REJECTED")) {
                                //responseMessage.result().replyAndRequest("REJECTED");
                            } else if(state.equalsIgnoreCase("IN_PROGRESS")){
                                this.logger.info("Entro IN_PROGRESS");
                            }*/
                        }
                    });
                } else {
                    messageHandler.replyAndRequest(new JsonObject().put("result","error"));
                }
            });
        });
    }

    public Future<JsonObject> createOrder(WebClient webClient, OrderCreate orderCreate){
        Handler<Promise<JsonObject>> request = futere -> {
            String urlOrder = "/service-order-manager/api/service-orders";
            OrderAdd order = new OrderAdd();
            order.setExternalId("DSD-TEST-SUPERMAKET");
            order.setPriority(4);
            order.setDescription("TEST ORDER ORDER");
            order.setCategory("OTT_SERVICE");
            order.setRegion("GUATEMALA");
            order.setSource("4-GT-SYM");
            order.setOrderType("PROVIDE");
            OrderItem orderItem = new OrderItem();
            orderItem.setId(1);
            orderItem.setAction("ADD");
            Service service = new Service();
            ServiceSpecification serviceSpecification = new ServiceSpecification();
            serviceSpecification.setSource("SYM-CATALOG");
            serviceSpecification.setName("OTT_SERVICE_CFS");
            serviceSpecification.setCode("OTT_SERVICE_CFS");
            serviceSpecification.setVersion("1.0");
            service.setServiceSpecification(serviceSpecification);
            service.setCharacteristics(new ArrayList<>());
            service.getCharacteristics().add(new Characteristic("SUPPLIER","VIX"));
            service.getCharacteristics().add(new Characteristic("MSISDN","50233124569"));
            service.getCharacteristics().add(new Characteristic("SKU","tigo-gt-web-1mo"));

            ArrayList<CharacteristicRelationship> characteristicRelationships = new ArrayList<CharacteristicRelationship>();
            characteristicRelationships.add(new CharacteristicRelationship("VALUE","GT"));
            service.getCharacteristics().add(new Characteristic("CUSTOM_INFO","ORGANIZATION_CODE", characteristicRelationships));

            characteristicRelationships = new ArrayList<CharacteristicRelationship>();
            characteristicRelationships.add(new CharacteristicRelationship("VALUE","MOBILE"));
            service.getCharacteristics().add(new Characteristic("CUSTOM_INFO","LINE_TYPE", characteristicRelationships));

            characteristicRelationships = new ArrayList<CharacteristicRelationship>();
            characteristicRelationships.add(new CharacteristicRelationship("VALUE","925"));
            service.getCharacteristics().add(new Characteristic("CUSTOM_INFO","BILLING_ID", characteristicRelationships));

            service.setPublicIdentifier("GT-50233124569_tigo-gt-web-1mo_OTT_SERVICE_VIX");
            orderItem.setService(service);
            order.setOrderItems(new ArrayList<OrderItem>());
            order.getOrderItems().add(orderItem);
            order.setRelatedParty(new ArrayList<RelatedParty>());
            RelatedParty relatedParty = new RelatedParty("GT-50233124569","BSS","GT-50233124569","CUSTOMER");
            order.getRelatedParty().add(relatedParty);

            this.webClient.post(443,"gt.qasymphonica.tigo.com",urlOrder)
                    .ssl(true)
                    .timeout(1000)
                    .putHeader("X-Organization-Code","TIGOGT")
                    .putHeader("X-Authorization",orderCreate.getToken())
                    .putHeader("Content-Type","application/iway-service-order-post-v1-hal+json")
                    .sendJson(order)
                    .onComplete(response -> {
                        if(response.succeeded()){
                            futere.complete(new JsonObject().put("orderId",response.result().bodyAsJsonObject().getValue("id").toString()));
                        } else {
                            futere.fail("fail");
                        }
                    });
        };
        return this.circuitBreaker.execute(request);
    }
}

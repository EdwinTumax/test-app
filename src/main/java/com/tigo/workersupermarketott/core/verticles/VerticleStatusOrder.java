package com.tigo.workersupermarketott.core.verticles;

import com.google.gson.Gson;
import com.tigo.workersupermarketott.core.domain.models.OrderCreate;
import com.tigo.workersupermarketott.core.domain.models.symphonica.auth.Token;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class VerticleStatusOrder extends AbstractVerticle {

    private static final String EVENT_BUS_STATUS_ORDER = "EVENT_BUS_STATUS_ORDER";
    private static final String CIRCUIT_BREAKER_STATUS_ORDER = "CIRCUIT_BREAKER_STATUS_ORDER";
    private static final String EVENT_BUS_CREATE_TOKEN = "EVENT_BUS_CREATE_TOKEN";

    @Autowired
    private Gson gson;

    private EventBus eventBus;
    private WebClient webClient;
    private CircuitBreaker circuitBreaker;

    private static final Logger logger = LoggerFactory.getLogger(VerticleStatusOrder.class);
    int intentos = 0;

    @Override
    public void start(){
        this.eventBus = vertx.eventBus();
        this.webClient = WebClient.create(vertx);
        this.circuitBreaker = CircuitBreaker.create(this.CIRCUIT_BREAKER_STATUS_ORDER,vertx,new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setTimeout(8500)
                        .setFallbackOnFailure(true)
                        .setResetTimeout(10000))
                .openHandler(circuit-> { this.logger.info("Circuit breaker opened"); })
                .closeHandler(circuit -> { this.logger.info("Circuit breaker closed");})
                .halfOpenHandler(circuit -> { this.logger.warn("Circuit breaker half-opened");});

        this.eventBus.consumer(this.EVENT_BUS_STATUS_ORDER, orderHandler -> {
            JsonObject order = new JsonObject(orderHandler.body().toString());
            OrderCreate orderCreate = order.mapTo(OrderCreate.class);
            this.getStatusOrder(orderCreate).onComplete(response -> {
                if(response.succeeded()){
                    orderHandler.replyAndRequest(orderCreate);
                } else {
                    orderHandler.replyAndRequest("fail", replyHandler -> {
                        logger.info("response IN_PROGRESSS");
                    });
                }
            });
        });

        /*this.eventBus.consumer(this.EVENT_BUS_STATUS_ORDER, orderHandler -> {
            JsonObject order = new JsonObject(orderHandler.body().toString());
            OrderCreate orderCreate = order.mapTo(OrderCreate.class);
            this.logger.info(orderCreate.getOrderId());
            this.circuitBreaker.execute(promise -> {
                this.logger.info("Entra al circuito");
                String urlOrder = "/service-order-manager/api/service-orders/".concat(orderCreate.getOrderId());
                this.webClient.get(443, "gt.qasymphonica.tigo.com", urlOrder)
                        .ssl(true)
                        .timeout(1000)
                        .putHeader("X-Organization-Code", "TIGOGT")
                        .putHeader("X-Authorization", orderCreate.getToken())
                        .send().onComplete(responseWeb -> {
                            //if(responseWeb.succeeded()){
                                if(responseWeb.result().bodyAsJsonObject().getValue("state").toString().equalsIgnoreCase("IN_PROGRESS")){
                                    this.logger.error(responseWeb.result().bodyAsJsonObject().getValue("state").toString());
                                    promise.fail(gson.toJson(orderCreate));
                                } else {
                                    this.logger.info(responseWeb.result().bodyAsJsonObject().getValue("state").toString());
                                    promise.complete(responseWeb.result().bodyAsJsonObject());
                                }
                           // }
                        });
            }).onComplete(response -> {
                if(response.succeeded()){
                    orderHandler.replyAndRequest(response.result());
                } else if(response.failed()) {
                    orderHandler.reply(orderHandler.body());
                    this.logger.info(response.cause().toString());
                }
            });
        });*/


                /*this.webClient.get(443, "gt.qasymphonica.tigo.com", urlOrder)
                        .ssl(true)
                        .timeout(1000)
                        .putHeader("X-Organization-Code", "TIGOGT")
                        .putHeader("X-Authorization", orderCreate.getToken())
                        .send().onComplete(responseWeb -> {
                            //if(responseWeb.succeeded()){
                                if(responseWeb.result().bodyAsJsonObject().getValue("state").toString().equalsIgnoreCase("IN_PROGRESS")){
                                    this.logger.error(responseWeb.result().bodyAsJsonObject().getValue("state").toString());
                                    fut.fail("fail");
                                } else {
                                    this.logger.info(responseWeb.result().bodyAsJsonObject().getValue("state").toString());
                                    fut.complete(responseWeb.result().bodyAsJsonObject());
                                }
                           // }
                        });
            }).onComplete(response -> {
                if(response.succeeded()){
                    orderHandler.replyAndRequest(response.result());
                } else if(response.failed()) {
                    orderHandler.reply(orderHandler.body());
                    this.logger.info(response.cause().toString());
                }
            });
        });*/

        /*this.circuitBreaker.execute(future -> {
                    this.eventBus.consumer(this.EVENT_BUS_STATUS_ORDER, orderHandler -> {
                        JsonObject order = new JsonObject(orderHandler.body().toString());
                        OrderCreate orderCreate = order.mapTo(OrderCreate.class);
                        String urlOrder = "/service-order-manager/api/service-orders/".concat(orderCreate.getOrderId());
                        Future<HttpResponse<Buffer>> fut = this.webClient.get(443, "gt.qasymphonica.tigo.com", urlOrder)
                                .ssl(true)
                                .timeout(1000)
                                .putHeader("X-Organization-Code", "TIGOGT")
                                .putHeader("X-Authorization", orderCreate.getToken())
                                .send().onSuccess(response -> {
                                        this.logger.info("Petición en la web");
                                            //future.complete(new JsonObject().put("state",response.result().bodyAsJsonObject().getValue("state").toString()));
                                }).onFailure(error -> {

                                });
                        fut.onComplete(responseWeb -> {
                            if(responseWeb.succeeded()){
                                orderHandler.replyAndRequest(responseWeb.result().bodyAsJsonObject());
                            }
                        });
                    });
        });*/

        /*this.eventBus.consumer(this.EVENT_BUS_STATUS_ORDER, orderHandler -> {
            //this.eventBus.request(this.EVENT_BUS_CREATE_TOKEN,null, messageToken -> {
            JsonObject order = new JsonObject(orderHandler.body().toString());
            OrderCreate orderCreate = order.mapTo(OrderCreate.class);
                //Token token = Json.decodeValue(messageStatusOrder.result().body().toString(),Token.class);
                //JsonObject orderId = new JsonObject(Json.encode(messageStatusOrder.body()));
                createOrder(orderCreate).onComplete(responseStatus ->{
                    //this.logger.info("order status ".concat(responseStatus.result().toString()));
                    if(responseStatus.succeeded()){
                        this.logger.info(responseStatus.result().toString());
                        String state = new JsonObject(Json.encode(responseStatus.result())).getValue("state").toString();
                        if(state.equalsIgnoreCase("IN_PROGRESS") || state.equalsIgnoreCase("PENDING") ||
                                state.equalsIgnoreCase("ACKNOWLEDGE")){
                            this.logger.info("Entro IN_PROGRESS");
                            orderHandler.reply(responseStatus.result());
                        } else  {
                            orderHandler.replyAndRequest(responseStatus.result());
                        }
                    } else {
                        this.logger.error("Error");
                    }
                });
            //});

        });*/

    }


    public Future<JsonObject> getStatusOrder(OrderCreate orderCreate){
        Handler<Promise<JsonObject>> request = promise -> {
            String urlOrder = "/service-order-manager/api/service-orders/".concat(orderCreate.getOrderId());
            this.webClient.get(443,"gt.qasymphonica.tigo.com", urlOrder)
                    .ssl(true)
                    .timeout(1000)
                    .putHeader("X-Organization-Code","TIGOGT")
                    .putHeader("X-Authorization",orderCreate.getToken())
                    .send(result -> {
                        if(result.succeeded()){
                           if(result.result().bodyAsJsonObject().getValue("state").toString().equalsIgnoreCase("IN_PROGRESS")){
                               promise.fail(gson.toJson(orderCreate));
                           } else {
                               this.logger.info(result.result().bodyAsString());
                               promise.complete(result.result().bodyAsJsonObject());
                           }
                        } else {
                            promise.fail(gson.toJson(orderCreate));
                        }
                    });
        };
        return this.circuitBreaker.execute(request);
    }


    public Future<JsonObject> createOrder(OrderCreate orderCreate){
        Handler<Promise<JsonObject>> request = futere -> {
            String urlOrder = "/service-order-manager/api/service-orders/".concat(orderCreate.getOrderId());
            this.webClient.get(443,"gt.qasymphonica.tigo.com", urlOrder)
                    .ssl(true)
                    .timeout(1000)
                    .putHeader("X-Organization-Code","TIGOGT")
                    .putHeader("X-Authorization",orderCreate.getToken())
                    .send().onComplete(response -> {
                        this.logger.info("Petición en la web");
                        if(response.succeeded()) {
                            /*String state = response.result().bodyAsJsonObject().getValue("state").toString();
                            if(state.equalsIgnoreCase("IN_PROGRESS") || state.equalsIgnoreCase("PENDING") ||
                                    state.equalsIgnoreCase("ACKNOWLEDGE")) {
                                this.logger.info("Circuit breaker ".concat(state));
                                futere.fail("fail");
                            } else if(state.equalsIgnoreCase("COMPLETED")) {
                                this.logger.info(response.result().bodyAsString());
                                futere.complete(response.result().bodyAsJsonObject());
                            }*/
                            futere.complete(new JsonObject().put("state",response.result().bodyAsJsonObject().getValue("state").toString()));
                        } else {
                            futere.fail("fail");
                        }
                    });
        };
        return this.circuitBreaker.execute(request);
    }

}

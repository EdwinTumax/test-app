package com.tigo.workersupermarketott.core.verticles;

import com.google.gson.Gson;
import com.tigo.workersupermarketott.core.domain.models.symphonica.auth.Token;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.CachingWebClient;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VerticleTokenSymphonica extends AbstractVerticle {
    @Autowired
    private Gson gson;
    private WebClient webClient;
    private EventBus eventBus;
    private static final String CIRCUIT_BREAKER_TOKEN = "CIRCUIT_BREAKER_TOKEN";
    private static final String EVENT_BUS_CREATE_TOKEN = "EVENT_BUS_CREATE_TOKEN";
    private CircuitBreaker circuitBreaker;
    private static final Logger logger = LoggerFactory.getLogger(VerticleApiSupermarket.class);

    @Override
    public void start(){
        this.eventBus = vertx.eventBus();
        this.webClient = WebClient.create(vertx);
        this.circuitBreaker = CircuitBreaker.create(this.CIRCUIT_BREAKER_TOKEN,vertx,new CircuitBreakerOptions()
                .setMaxFailures(5)
                .setTimeout(5000)
                .setFallbackOnFailure(true)
                .setResetTimeout(10000))
                .openHandler(circuit-> { this.logger.info("Circuit breaker opened"); })
                .closeHandler(circuit -> { this.logger.info("Circuit breaker closed");})
                .halfOpenHandler(circuit -> { this.logger.warn("Circuit breaker half-opened");});
        this.eventBus.consumer(this.EVENT_BUS_CREATE_TOKEN, messageHandler -> {
            //this.logger.info("1 ".concat(messageHandler.body().toString()));
            this.getToken(this.webClient).onComplete(response -> {
                messageHandler.replyAndRequest(response.result());
            });
        });
    }

    public Future<JsonObject> getToken(WebClient webClient){
        MultiMap form = MultiMap.caseInsensitiveMultiMap();
        form.set("username","tigogtapi");
        form.set("password","jW8EQKwu");
        Handler<Promise<JsonObject>> request = future -> {
            String urlToken = "/sso_rest/authentication/login";
            CachingWebClientOptions options = new CachingWebClientOptions()
                    .addCachedMethod(HttpMethod.POST)
                            .removeCachedStatusCode(401)
                                    .setEnableVaryCaching(true);
            WebClient cachingWebClient = CachingWebClient.create(webClient,options);
            cachingWebClient.post(443, "gt.qasymphonica.tigo.com",urlToken)
                    .ssl(true)
                    .timeout(10000)
                    .putHeader("Content-Type","application/x-www-form-urlencoded")
                    .putHeader("X-Organization-Code","TIGOGT")
                    .sendForm(form)
                    .onComplete(response -> {
                        if(response.succeeded()){
                            Token token = gson.fromJson(response.result().bodyAsString(),Token.class);
                            HttpResponse<Buffer> tokenResponse = response.result();
                            if(tokenResponse.statusCode() == 200){
                                //future.complete(new JsonObject().put("statusCode",tokenResponse.statusCode()).put("token",token.getEmbedded().getSession().getToken()));
                                future.complete(new JsonObject(Json.encode(token)));
                            }
                        } else {
                            JsonObject jsonObject = new JsonObject()
                                    .put("statusCode","503");
                            future.fail(gson.toJson(jsonObject));
                        }
                    });
        };
        return this.circuitBreaker.execute(request);
    }
}

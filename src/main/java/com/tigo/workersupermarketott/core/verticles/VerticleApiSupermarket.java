package com.tigo.workersupermarketott.core.verticles;
import com.google.gson.Gson;
import com.tigo.workersupermarketott.core.domain.models.*;
import com.tigo.workersupermarketott.core.domain.models.symphonica.auth.Token;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VerticleApiSupermarket extends AbstractVerticle {

    private static final String EVENT_BUS_CREATE_TOKEN = "EVENT_BUS_CREATE_TOKEN";
    private static final String EVENT_BUS_CREATE_ORDER = "EVENT_BUS_CREATE_ORDER";
    private static final String EVENT_BUS_STATUS_ORDER = "EVENT_BUS_STATUS_ORDER";


    private EventBus eventBus;

    @Autowired
    private Gson gson;

    private static final Logger logger = LoggerFactory.getLogger(VerticleApiSupermarket.class);

    @Override
    public void start(){
        this.eventBus = vertx.eventBus();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route(HttpMethod.POST,"/v1/supermarket/subscription/:msisdn/package/:package/sourcesystem/mobile/category/:type").handler(ctx -> {
            OrderCreate orderCreate = new OrderCreate();
            orderCreate.setMsisdn(ctx.pathParam("msisdn"));
            orderCreate.setProductId(ctx.pathParam("package"));
            orderCreate.setType(ctx.pathParam("type"));
            DeliveryOptions options = new DeliveryOptions();
            options.addHeader("count","1");
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type","text/plain");
            response.end("Datos recibidos ".concat(orderCreate.getMsisdn()).concat(" ").concat(orderCreate.getProductId()).concat(" ").concat(orderCreate.getType()));
            this.vertx.eventBus().request(this.EVENT_BUS_CREATE_TOKEN,orderCreate,options, responseMessage ->{
                if(responseMessage.succeeded()){
                    this.eventBus.request(this.EVENT_BUS_CREATE_ORDER,responseMessage.result().body(), messageOrderReply -> {
                        if(messageOrderReply.succeeded()) {
                            this.eventBus.request(this.EVENT_BUS_STATUS_ORDER,messageOrderReply.result().body(), statusResult -> {
                                if(statusResult.succeeded()){
                                    String state = new JsonObject(Json.encode(statusResult.result().body())).getValue("state").toString();
                                    if(state.equalsIgnoreCase("COMPLETED")){
                                        responseMessage.result().replyAndRequest("COMPLETED");
                                    } else if (state.equalsIgnoreCase("FAILED")){
                                        responseMessage.result().replyAndRequest("FAILED");
                                    } else if (state.equalsIgnoreCase("PARTIAL")){
                                        responseMessage.result().replyAndRequest("CANCELLED");
                                    } else if(state.equalsIgnoreCase("CANCELLED")){
                                        responseMessage.result().replyAndRequest("CANCELLED");
                                    } else if(state.equalsIgnoreCase("REJECTED")) {
                                        responseMessage.result().replyAndRequest("REJECTED");
                                    } else {

                                    }
                                } else {

                                }
                            });
                        } else {
                            this.logger.error(messageOrderReply.result().body().toString());
                        }
                    });
                } else {
                    logger.error(responseMessage.result().body().toString());
                }
            });
        });

        router.route(HttpMethod.POST, "/v2/supermarket/subscription/:msisdn/package/:packageid/sourcesystem/:sourcesystemid/category/:category").handler(ctx -> {
            OrderCreate orderCreate = new OrderCreate();
            orderCreate.setMsisdn(ctx.pathParam("msisdn"));
            orderCreate.setProductId(ctx.pathParam("packageid"));
            orderCreate.setCategory("category");
            orderCreate.setType("add");
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type","application/json");
            this.vertx.eventBus().request(this.EVENT_BUS_CREATE_TOKEN, orderCreate,tokenHandler -> {
                if(tokenHandler.succeeded()){
                    Token token = Json.decodeValue(tokenHandler.result().body().toString(),Token.class);
                    orderCreate.setToken(token.getEmbedded().getSession().getToken());
                    this.eventBus.request(this.EVENT_BUS_CREATE_ORDER,orderCreate,replyOrder -> {
                        this.logger.info(replyOrder.result().body().toString());
                        if(replyOrder.succeeded()){
                            /*JsonObject jsonOrder = new JsonObject(replyOrder.result().body().toString());
                            this.logger.info("Final ".concat(jsonOrder.toString()));
                            OrderCreateSuccessResponse orderCreateSuccessResponse = new OrderCreateSuccessResponse();
                            Attribute attribute = new Attribute();
                            attribute.setId(jsonOrder.getString("id"));
                            attribute.setExternalId(jsonOrder.getString("externalId"));
                            attribute.setOrderDate(jsonOrder.getString("orderDate"));
                            attribute.setPublicIdentifier("Pending");
                            attribute.setMessage("Orden enviada exitosamente");
                            orderCreateSuccessResponse.setData(new Data(attribute))*/
                            OrderCreateErrorResponse orderCreateSuccessResponse = new OrderCreateErrorResponse();
                            orderCreateSuccessResponse.setErrorCode(200);
                            orderCreateSuccessResponse.setErrorType("INFO");
                            orderCreateSuccessResponse.setCode("201");
                            orderCreateSuccessResponse.setDescription("SUCCESS ORDEN");;
                            response.end(Json.encode(orderCreateSuccessResponse));
                        } else {
                            OrderCreateErrorResponse orderCreateErrorResponse = new OrderCreateErrorResponse();
                            orderCreateErrorResponse.setErrorCode(400);
                            orderCreateErrorResponse.setErrorType("NEG");
                            orderCreateErrorResponse.setCode("400");
                            orderCreateErrorResponse.setDescription("Error al crear la orden");
                            response.end(Json.encode(orderCreateErrorResponse));
                        }
                    });
                } else {
                    OrderCreateErrorResponse orderCreateErrorResponse = new OrderCreateErrorResponse();
                    orderCreateErrorResponse.setErrorCode(400);
                    orderCreateErrorResponse.setErrorType("NEG");
                    orderCreateErrorResponse.setCode("400");
                    orderCreateErrorResponse.setDescription("Error al crear la orden");
                    response.end(Json.encode(orderCreateErrorResponse));
                }
            });
        });
        server.requestHandler(router).listen(9081);
    }


}

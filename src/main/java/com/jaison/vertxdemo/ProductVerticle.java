package com.jaison.vertxdemo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.nio.file.NoSuchFileException;
import java.util.UUID;

public class ProductVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductVerticle.class);

  @Override
  public void start() {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    router.put("/products").handler(this::handleAddProduct);
    router.get("/products/:productId").handler(this::handleGetProduct);
    router.get("/products").handler(this::handleListProducts);


    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    LOGGER.info("ProductVerticle Deployed");
  }
  

  private void handleAddProduct(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    if(routingContext.getBodyAsString().isEmpty()){
      sendError(400, response);
      return;
    }
    JsonObject product;
    try {
      product = routingContext.getBodyAsJson();
    } catch (Exception e){
      sendError(400, response);
      return;
    }
    String id = UUID.randomUUID().toString();
    product.put("id",id);

    Buffer buffer = Buffer.buffer(product.encodePrettily());
    vertx.fileSystem().writeFile("./data/"+id+".json", buffer,
        result -> {
          if(result.succeeded()) {
            response.putHeader("content-type", "application/json").end(product.encodePrettily());
          } else {
            LOGGER.error("Failed to persist to the data store", result.cause());
            sendError(500, response);
          }
        });
  }

  private void handleGetProduct(RoutingContext routingContext) {
    String productId = routingContext.request().getParam("productId");
    HttpServerResponse response = routingContext.response();

    vertx.fileSystem().readFile("./data/"+productId+".json",
        result -> {
          if(result.succeeded()) {
            response.putHeader("content-type", "application/json").end(result.result());
          } else if(result.cause().getCause().getClass().equals(NoSuchFileException.class)) {
            sendError(404, response);
          } else {
            LOGGER.error("Failed to get the Product", result.cause());
            sendError(500, response);
          }
        });
  }

  private void handleListProducts(RoutingContext routingContext) {
    JsonArray arr = new JsonArray();
    HttpServerResponse response = routingContext.response();

    vertx.fileSystem().readDir("./data/",
        result -> {
          if(result.succeeded()) {
            response.putHeader("content-type", "application/json").end(result.result().get(0));
          } else {
            LOGGER.error("Failed to Look up all products", result.cause());
            sendError(500, response);
          }
        });
  }

  private void sendError(int statusCode, HttpServerResponse response) {
    response.setStatusCode(statusCode).end();
  }


}
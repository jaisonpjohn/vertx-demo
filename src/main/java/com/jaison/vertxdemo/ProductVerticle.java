package com.jaison.vertxdemo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductVerticle.class);

  //TODO: move this to config rather than constant
  private static final String DATA_DIR = "./data/";

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

    Buffer buffer = Buffer.buffer(product.encode());
    vertx.fileSystem().writeFile(getFilePath(id), buffer,
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

    vertx.fileSystem().readFile(getFilePath(productId),
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
    JsonArray jsonArray = new JsonArray();
    HttpServerResponse response = routingContext.response();

    // TODO: introduce RxJava and Observable to avoid callbackhell
    vertx.fileSystem().readDir(DATA_DIR,
        result -> {
          if(result.succeeded()) {
            List<Future> futures = new ArrayList<>();
            result.result().forEach(filePath->
            {
              Future fileFuture = Future.future();
              futures.add(fileFuture);
              vertx.fileSystem().readFile(filePath,fileResult -> {
                if(fileResult.succeeded()) {
                  jsonArray.add(new JsonObject(fileResult.result()));
                }
                fileFuture.complete();
              });
            });
            CompositeFuture compositeFuture = CompositeFuture.all(futures).setHandler(fileFutureResult->{
              response.putHeader("content-type", "application/json").end(jsonArray.encodePrettily());
            });

          } else {
            LOGGER.error("Failed to Look up all products", result.cause());
            sendError(500, response);
          }
        });
  }

  private void sendError(int statusCode, HttpServerResponse response) {
    response.setStatusCode(statusCode).end();
  }

  private String getFilePath (String prodId){
    return DATA_DIR+prodId+".json";
  }

}
package com.jaison.vertxdemo;

import com.google.inject.Guice;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Inject
  private ProductHandlerVerticle productHandlerVerticle;

  @Override
  public void start() {

    ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
        .setType("file")
        .setFormat("yaml")
        .setConfig(new JsonObject()
            .put("path", "application.yml")
        );

    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(defaultConfig)
        /*.addStore(new ConfigStoreOptions().setType("sys"))*/
        .addStore(new ConfigStoreOptions().setType("env"))
    );

    retriever.getConfig(ar -> {
      if (ar.failed()) {
        LOGGER.error("Error in reading config",ar.cause());
      } else {
        JsonObject config = ar.result();

        //Binds all dependencies to already initialized vertx instance
        Guice.createInjector(new BindingModule(vertx,config)).injectMembers(this);

        LOGGER.info("Read Config, now deploying Main verticle");
        vertx.deployVerticle(productHandlerVerticle,
            stringAsyncResult -> {
              if(stringAsyncResult.succeeded()){
                System.out.println("ProductHandler deployment completed");
              }else{
                System.out.println("ProductHandler deployment FAILED!"+stringAsyncResult.cause().getMessage());
              }
            });
        LOGGER.info("MainVerticle Deployed");
      }
    });

  }

}
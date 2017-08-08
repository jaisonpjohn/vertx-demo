package com.jaison.vertxdemo;

import io.vertx.core.Vertx;

import java.util.function.Consumer;


public class Runner {
  public static void main(String[] args) {
    Consumer<Vertx> runner = vertx -> {
      try {
        vertx.deployVerticle(ProductVerticle.class.getName(),
            stringAsyncResult -> System.out.println("ProductVerticle deployment completed"));
      } catch (Throwable t) {
        t.printStackTrace();
      }
    };
    Vertx vertx = Vertx.vertx();
    runner.accept(vertx);
  }

}
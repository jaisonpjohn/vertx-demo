package com.jaison.vertxdemo;

import io.vertx.core.Vertx;


public class Runner {
  public static void main(String[] args) {
    try {
      Vertx.vertx().deployVerticle(MainVerticle.class.getName(),
          stringAsyncResult -> {
            if(stringAsyncResult.succeeded()){
              System.out.println("MainVerticle deployment completed");
            }else{
              System.out.println("MainVerticle deployment FAILED!"+stringAsyncResult.cause().getMessage());
            }
          });
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}
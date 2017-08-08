package com.jaison.vertexdemo;

import com.jaison.vertxdemo.ProductVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ProductVerticleTest {

  private Vertx vertx;

  @Rule
  public Timeout rule = Timeout.seconds(2);

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(ProductVerticle.class.getName(), tc.asyncAssertSuccess());
    //vertx.exceptionHandler(tc.exceptionHandler());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void http404isThrownWhenAccessingAnUnhandledUrlPattern(TestContext tc) {
    vertx.createHttpClient().getNow(8080, "localhost", "/aURLThatDoesntExist", response -> {
      tc.assertEquals(response.statusCode(), 404);
    });
  }

  @Test
  public void http200isTheStatusCodeWhenRequestingAListOfProducts(TestContext tc) {
    Async async = tc.async();
    vertx.createHttpClient().getNow(8080, "localhost", "/products", response -> {
      tc.assertEquals(response.statusCode(), 200);
      response.bodyHandler(body -> {
        tc.assertTrue(body.length() > 0);
        async.complete();
      });
    });
  }

  @Test
  public void http400isThrownWhenPUTanEmptyBody(TestContext tc) {
    Async async = tc.async();
    vertx.createHttpClient()
        .put(8080, "localhost", "/products", resp -> {
      tc.assertEquals(400, resp.statusCode());
      async.complete();
    }).end();
  }

  @Test
  public void http400isThrownWhenPUTaNonJsonStringAsBody(TestContext tc) {
    Async async = tc.async();
    String body = "ANon-JSONString";
    vertx.createHttpClient()
        .put(8080, "localhost", "/products", resp -> {
          tc.assertEquals(400, resp.statusCode());
          async.complete();
        })
        .putHeader("content-length", String.valueOf(body.length()))
        .putHeader("content-type", "application/json")
        .write(body)
        .end();
  }

  @Test
  public void http200isTheStatusCodeWhenAddedAProductSuccessfully(TestContext tc) {
    Async async = tc.async();
    String requestBody = "{\"name\" : \"Egg Whisk\", \"price\" : 3.99, \"weight\" : 150 }";
    vertx.createHttpClient()
        .put(8080, "localhost", "/products", resp -> {
          tc.assertEquals(200, resp.statusCode());
          async.complete();
        })
        .putHeader("content-length", String.valueOf(requestBody.length()))
        .putHeader("content-type", "application/json")
        .write(requestBody)
        .end();
  }

  @Test
  public void anIdIsGeneratedAndReturnedWhenAddedAProductSuccessfully(TestContext tc) {
    Async async = tc.async();
    String requestBody = "{\"name\" : \"Egg Whisk\", \"price\" : 3.99, \"weight\" : 150 }";
    vertx.createHttpClient()
        .put(8080, "localhost", "/products", resp -> {
          resp.bodyHandler(body -> {
            tc.assertNotNull(body.toJsonObject().getString("id"));
            async.complete();
          });
        })
        .putHeader("content-length", String.valueOf(requestBody.length()))
        .putHeader("content-type", "application/json")
        .write(requestBody)
        .end();
  }
}
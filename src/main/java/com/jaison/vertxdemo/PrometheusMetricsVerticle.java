package com.jaison.vertxdemo;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.vertx.MetricsHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

import javax.inject.Singleton;

@Singleton
public class PrometheusMetricsVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate("exported");
    CollectorRegistry.defaultRegistry.register(new DropwizardExports(metricRegistry));

    //Bind metrics handler to /metrics
    Router router = Router.router(vertx);
    router.get("/metrics").handler(new MetricsHandler());

    //Start httpserver on localhost:8080
    vertx.createHttpServer().requestHandler(router::accept).listen(8080);

    //Increase counter every second
    vertx.setPeriodic(1_000L, e -> metricRegistry.counter("testCounter").inc());
  }
}

package vertx.web.demo;

import io.prometheus.client.Counter;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.vertx.MetricsHandler;
import io.vertx.core.*;
import io.vertx.core.eventbus.*;
import io.vertx.core.json.*;
import io.vertx.ext.auth.oauth2.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class Main extends AbstractVerticle {

  @Override
  public void start() {
    DefaultExports.initialize();
    final EventBus eb = vertx.eventBus();
    final Router router = Router.router(vertx);

    router.get("/metrics").handler(new MetricsHandler());
    router.route().handler(LoggerHandler.create());

    BridgeOptions opts = new BridgeOptions()
      .addOutboundPermitted(
        new PermittedOptions().setAddress("javazone.data.updates")
      );

    router.route("/eventbus/*").handler(
      SockJSHandler.create(vertx).bridge(opts));

    final Counter counter = Counter.build()
      .name("api_calls").help("api_calls").register();

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(
      LocalSessionStore.create(vertx)
    ));

    OAuth2Auth oauth2 = OAuth2Auth
      .createKeycloak(vertx, OAuth2FlowType.AUTH_CODE, config());

    router.route().handler(UserSessionHandler.create(oauth2));

    OAuth2AuthHandler authHandler = OAuth2AuthHandler
      .create(oauth2, "http://jetdrone");

    authHandler.setupCallback(router.route("/callback"));
    router.route("/private/*").handler(authHandler);

    router.get("/utx/:skip/:limit").handler(ctx -> {
      counter.inc();

      try {
        JsonObject options = new JsonObject()
          .put("skip", Integer.parseInt(ctx.request().getParam("skip")))
          .put("limit", Integer.parseInt(ctx.request().getParam("limit")));

        eb.send("javazone.storage.find", options,  send -> {
          if (send.failed()) {
            ctx.fail(send.cause());
          } else {
            ctx.response()
              .putHeader("content-type", "application/json")
              .end(((JsonArray) send.result().body()).encode());
          }
        });
      } catch (NumberFormatException e) {
        ctx.fail(e);
      }
    });

    router.route().handler(StaticHandler.create());

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(8000);
  }
}

package vertx.storage.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

public class Main extends AbstractVerticle {

  private final Logger LOG = LoggerFactory.getLogger(Main.class);

  @Override
  public void start() {
    final EventBus eb = vertx.eventBus();
    final MongoClient mongo = MongoClient.createShared(vertx,
      new JsonObject()
        .put("connection_string", "mongodb://mongo:27017")
        .put("db_name", "blockchain"));

    vertx.eventBus().consumer("javazone.storage.save", msg -> {
      mongo.insert("utx", (JsonObject) msg.body(), insert -> {
        if (insert.succeeded()) {
          LOG.info("SAVE OK");
          eb.publish("javazone.data.updates", msg.body());
        } else {
          LOG.error("SAVE FAIL", insert.cause());
        }
      });
    });

    vertx.eventBus().consumer("javazone.storage.find", msg -> {
      FindOptions opts = new FindOptions((JsonObject) msg.body());
      mongo.findWithOptions("utx", new JsonObject(), opts, find -> {
        if (find.failed()) {
          LOG.error("FIND FAIL", find.cause());
          msg.fail(500, find.cause().getMessage());
        } else {
          LOG.info("FIND OK");
          msg.reply(new JsonArray(find.result()));
        }
      });
    });
  }
}

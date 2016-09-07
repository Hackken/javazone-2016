package basic

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._

class WebSimulation extends Simulation {

  val httpConf = http.baseURL("http://localhost")
    .disableCaching

  val scn = scenario("API Load Test").repeat(100) {
    exec(
      http("Page with 10 results")
        .get("/utx/0/10")
        .check(status.is(200))
    )
  }

  setUp(
    scn.inject(atOnceUsers(200))
  ).protocols(httpConf)
}

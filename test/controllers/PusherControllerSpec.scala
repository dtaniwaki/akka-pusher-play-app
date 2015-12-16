package controllers

import play.api.Configuration
import play.api.mvc._
import play.api.test._
import scala.concurrent.Future
import com.typesafe.config.{ Config, ConfigFactory }

object PusherControllerSpec extends PlaySpecification with Results {
  val config = Configuration(ConfigFactory.load())

  "PusherController#index" should {
    "should be valid" in {
      val controller = new PusherController(config)
      val result: Future[Result] = controller.index().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText must contain("pusher")
    }
  }
}

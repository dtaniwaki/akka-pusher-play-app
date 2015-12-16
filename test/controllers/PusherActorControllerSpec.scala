package controllers

import akka.actor.ActorSystem
import com.typesafe.config.{ Config, ConfigFactory }
import play.api.Configuration
import play.api.mvc._
import play.api.test._
import scala.concurrent.Future

object PusherActorControllerSpec extends PlaySpecification with Results {
  val config = Configuration(ConfigFactory.load())

  "PusherActorController#index" should {
    "should be valid" in {
      implicit val system = ActorSystem("test")
      val controller = new PusherActorController(config)
      val result = controller.index().apply(FakeRequest())
      val bodyText = contentAsString(result)
      bodyText must contain("pusher")
    }
  }
}

package controllers

import play.api.Configuration
import play.api.libs.json._
import play.api.mvc._
import play.api.test._
import scala.concurrent.Future
import com.typesafe.config.{ Config, ConfigFactory }

object PusherControllerSpec extends PlaySpecification with Results {
  val config = Configuration(ConfigFactory.load())
  val controller = new PusherController(config)

  "#index" should {
    "should be valid" in {
      val result: Future[Result] = controller.index().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText must contain("pusher")
    }
  }
  if(config.getString("pusher.appId").isDefined && config.getString("pusher.appId").get.nonEmpty) {
    "#trigger" should {
      "should be valid" in {
        val result: Future[Result] = controller.triggerAction().apply(FakeRequest(
          Helpers.POST,
          routes.PusherController.triggerAction().url,
          FakeHeaders(Seq(
            "Content-type" -> "application/json"
          )),
          Json.obj(
            "channel" -> "a_channel",
            "event" -> "a_event",
            "body" -> Json.obj(
              "foo" -> "bar"
            ).toString,
            "socket_id" -> "123.345"
          )
        ))
        val bodyText: String = contentAsString(result)
        Json.parse(bodyText) must be equalTo Json.obj(
          "data" -> "{}"
        )
      }
    }
    "#channel" should {
      "should be valid" in {
        val result: Future[Result] = controller.channelAction().apply(FakeRequest(
          Helpers.POST,
          routes.PusherController.channelAction().url,
          FakeHeaders(Seq(
            "Content-type" -> "application/json"
          )),
          Json.obj(
            "channel" -> "presence-channel"
          )
        ))
        val bodyText: String = contentAsString(result)
        Json.parse(bodyText) must be equalTo Json.obj(
          "occupied" -> false,
          "user_count" -> 0
        )
      }
    }
    "#channels" should {
      "should be valid" in {
        val result: Future[Result] = controller.channelsAction().apply(FakeRequest(
          Helpers.POST,
          routes.PusherController.channelsAction().url,
          FakeHeaders(Seq(
            "Content-type" -> "application/json"
          )),
          Json.obj(
            "prefix" -> "presence-prefix"
          )
        ))
        val bodyText: String = contentAsString(result)
        Json.parse(bodyText) must be equalTo Json.obj(
          "channels" -> Json.obj()
        )
      }
    }
    "#users" should {
      "should be valid" in {
        val result: Future[Result] = controller.usersAction().apply(FakeRequest(
          Helpers.POST,
          routes.PusherController.usersAction().url,
          FakeHeaders(Seq(
            "Content-type" -> "application/json"
          )),
          Json.obj(
            "channel" -> "presence-channel"
          )
        ))
        val bodyText: String = contentAsString(result)
        Json.parse(bodyText) must be equalTo Json.obj("users" -> Json.arr())
      }
    }
  } else {
    skipped("pusher.appId is not defined")
  }
}

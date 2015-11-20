package controllers

import com.github.dtaniwaki.akka_pusher.PusherModels.ChannelData
import com.github.dtaniwaki.akka_pusher.PusherRequests._
import com.github.dtaniwaki.akka_pusher.{PusherClient, PusherJsonSupport}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import play.api.mvc._
import spray.json._

class PusherController extends Controller
  with PusherHelper
  with PusherJsonSupport
{
  val pusherClient: PusherClient = new PusherClient()

  def authAction = Action(parse.urlFormEncoded) { implicit request =>
    val key = request.headers.get("X-Pusher-Key").get
    val signature = request.headers.get("X-Pusher-Signature").get
    val pusherRequest = Json.toJson(request.body).toString.parseJson.convertTo[AuthRequest]

    if (pusherClient.validateSignature(key, signature, request.body.toString)) {
      val res = pusherClient.authenticate(
        pusherRequest.channelName,
        pusherRequest.socketId,
        Some(ChannelData(userId = "dtaniwaki", userInfo = Some(Map("user_name" -> "dtaniwaki", "name" -> "Daisuke Taniwaki"))))
      )
      Ok(Json.toJson(res.toJson.toString))
    } else {
      Unauthorized(Json.toJson("{}"))
    }
  }

  def webhookAction = Action(parse.text) { implicit request =>
    request.body.toString.parseJson.convertTo[WebhookRequest].events foreach {
      case event =>
        logger.warn(s"Got event: $event")
    }
    Ok(Json.toJson("{}"))
  }

  def triggerAction = Action.async { implicit request =>
    val (channel, event, body) = triggerForm.get
    pusherClient.trigger(channel, event, body).map { res => Ok(Json.toJson(res.toJson.toString)) }
  }

  def channelAction = Action.async { implicit request =>
    val (channel) = channelForm.get
    pusherClient.channel(channel, Some(Seq("user_count"))).map { res => Ok(Json.toJson(res.toJson.toString)) }
  }

  def channelsAction = Action.async { implicit request =>
    val (prefix) = channelsForm.get
    pusherClient.channels(prefix, Some(Seq("user_count"))).map { res => Ok(Json.toJson(res.toJson.toString)) }
  }

  def usersAction = Action.async { implicit request =>
    val (channel) = usersForm.get
    pusherClient.users(channel).map { res => Ok(Json.toJson(res.toJson.toString)) }
  }
}

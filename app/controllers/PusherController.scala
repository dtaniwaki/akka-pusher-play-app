package controllers

import com.github.dtaniwaki.akka_pusher.PusherModels.ChannelData
import com.github.dtaniwaki.akka_pusher.PusherRequests._
import com.github.dtaniwaki.akka_pusher.{PusherClient, PusherJsonSupport}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import play.api.mvc._
import spray.json._
import scala.util.{Success, Failure}

class PusherController extends Controller
  with PusherHelper
  with PusherJsonSupport
{
  val pusherClient: PusherClient = new PusherClient()

  def authAction = Action(parse.urlFormEncoded) { implicit request =>
    val pusherRequest = Json.stringify(Json.toJson(request.body.toMap.mapValues(_(0)))).parseJson.convertTo[AuthRequest]

    val res = pusherClient.authenticate(
      pusherRequest.channelName,
      pusherRequest.socketId,
      Some(ChannelData(userId = "dtaniwaki", userInfo = Some(Map("user_name" -> "dtaniwaki", "name" -> "Daisuke Taniwaki"))))
    )
    Ok(Json.parse(res.toJson.toString))
  }

  def webhookAction = Action(parse.json) { implicit request =>
    val key = request.headers.get("X-Pusher-Key").get
    val signature = request.headers.get("X-Pusher-Signature").get

    if (pusherClient.validateSignature(key, signature, request.body.toString)) {
      val webhookRequest = Json.stringify(request.body).parseJson.convertTo[WebhookRequest]
      webhookRequest.events foreach {
        case event =>
          logger.warn(s"Got event: $event")
      }
      Ok(Json.toJson("{}"))
    } else {
      Unauthorized(Json.toJson("{}"))
    }
  }

  def triggerAction = Action.async(parse.json) { implicit request =>
    val (channel, event, body, socketId) = triggerForm.bindFromRequest.get
    pusherClient.trigger(channel, event, body, socketId).map {
      case Success(res) => Ok(Json.parse(res.toJson.toString))
      case Failure(e) => InternalServerError(e.getMessage)
    }
  }

  def channelAction = Action.async(parse.json) { implicit request =>
    val (channel) = channelForm.bindFromRequest.get
    pusherClient.channel(channel, Some(Seq("user_count"))).map {
      case Success(res) => Ok(Json.parse(res.toJson.toString))
      case Failure(e) => InternalServerError(e.getMessage)
    }
  }

  def channelsAction = Action.async(parse.json) { implicit request =>
    val (prefix) = channelsForm.bindFromRequest.get
    pusherClient.channels(prefix, Some(Seq("user_count"))).map {
      case Success(res) => Ok(Json.parse(res.toJson.toString))
      case Failure(e) => InternalServerError(e.getMessage)
    }
  }

  def usersAction = Action.async(parse.json) { implicit request =>
    val (channel) = usersForm.bindFromRequest.get
    pusherClient.users(channel).map {
      case Success(res) => Ok(Json.parse(res.toJson.toString))
      case Failure(e) => InternalServerError(e.getMessage)
    }
  }
}

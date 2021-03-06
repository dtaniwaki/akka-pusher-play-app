package controllers

import javax.inject.Inject

import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.github.dtaniwaki.akka_pusher.attributes.{PusherChannelAttributes, PusherChannelsAttributes}
import com.github.dtaniwaki.akka_pusher.PusherMessages._
import com.github.dtaniwaki.akka_pusher.PusherRequests._
import com.github.dtaniwaki.akka_pusher.{PusherModels, PusherActor, PusherJsonSupport}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{Configuration, Play}
import play.api.libs.json.Json
import play.api.mvc._
import spray.json._
import scala.concurrent.Future
import scala.util.{Success, Failure}

class PusherActorController @Inject()(config: Configuration = Play.current.configuration)(implicit system: ActorSystem) extends Controller
  with PusherHelper
  with PusherJsonSupport
{
  val pusherActor: ActorRef = system.actorOf(PusherActor.props, "pusher-actor")
  implicit val timeout = Timeout(5 seconds)

  def index = Action {
    Ok(views.html.shared.pusher(config.getString("pusher.key").get.trim)("pusher_actor"))
  }

  def authAction = Action.async(parse.urlFormEncoded) { implicit request =>
    val pusherRequest = Json.stringify(Json.toJson(request.body.toMap.mapValues(_(0)))).parseJson.convertTo[AuthRequest]

    (pusherActor ask AuthenticateMessage(
      pusherRequest.channelName,
      pusherRequest.socketId,
      Some(PusherModels.ChannelData(userId = "dtaniwaki", userInfo = Some(Map("user_name" -> "dtaniwaki", "name" -> "Daisuke Taniwaki").toJson)))
    )).map {
      case res: PusherModels.AuthenticatedParams =>
        Ok(Json.parse(res.toJson.toString))
    }
  }

  def webhookAction = Action.async(parse.json) { implicit request =>
    val key = request.headers.get("X-Pusher-Key").get
    val signature = request.headers.get("X-Pusher-Signature").get
    (pusherActor ask ValidateSignatureMessage(key, signature, request.body.toString)).flatMap {
      case Success(true) =>
        val webhookRequest = Json.stringify(request.body).parseJson.convertTo[WebhookRequest]
        webhookRequest.events foreach {
          case event =>
            logger.warn(s"Got event: $event")
        }
        Future(Ok(Json.toJson("{}")))
      case Success(false) =>
        Future(Unauthorized(Json.toJson("{}")))
      case Failure(e) => throw e
      case _ =>
        throw new RuntimeException("Unknown response from pusher")
    }
  }

  def triggerAction = Action.async(parse.json) { implicit request =>
    val (channel, event, body, socketId) = triggerForm.bindFromRequest.get
    (pusherActor ask TriggerMessage(channel, event, body.toJson, socketId)).map {
      case Success(res: PusherModels.Result) => Ok(Json.parse(res.toJson.toString))
      case Failure(e) => InternalServerError(e.getMessage)
      case _ => Ok(Json.parse("{}"))
    }
  }

  def channelAction = Action.async(parse.json) { implicit request =>
    val (channel) = channelForm.bindFromRequest.get
    (pusherActor ask ChannelMessage(channel, Seq(PusherChannelAttributes.userCount))).map {
      case Success(res: PusherModels.Channel) => Ok(Json.parse(res.toJson.toString))
      case Failure(e) => InternalServerError(e.getMessage)
    }
  }

  def channelsAction = Action.async(parse.json) { implicit request =>
    val (prefix) = channelsForm.bindFromRequest.get
    (pusherActor ask ChannelsMessage(prefix, Seq(PusherChannelsAttributes.userCount))).map {
      case Success(res: PusherModels.ChannelMap) =>
        Ok(Json.parse(res.toJson.toString))
      case Failure(e) => InternalServerError(e.getMessage)
    }
  }

  def usersAction = Action.async(parse.json) { implicit request =>
    val (channel) = usersForm.bindFromRequest.get
    (pusherActor ask UsersMessage(channel)).map {
      case Success(res: PusherModels.UserList) =>
        Ok(Json.parse(res.toJson.toString))
      case Failure(e) => InternalServerError(e.getMessage)
    }
  }
}

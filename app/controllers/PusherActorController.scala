package controllers

import javax.inject.Inject

import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.github.dtaniwaki.akka_pusher.PusherMessages._
import com.github.dtaniwaki.akka_pusher.PusherRequests._
import com.github.dtaniwaki.akka_pusher.{PusherModels, PusherActor, PusherJsonSupport}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import play.api.mvc._
import spray.json._
import scala.concurrent.Future

class PusherActorController @Inject() (implicit system: ActorSystem) extends Controller
  with PusherHelper
  with PusherJsonSupport
{
  val pusherActor: ActorRef = system.actorOf(PusherActor.props, "pusher-actor")
  implicit val timeout = Timeout(5 seconds)

  def authAction = Action.async(parse.urlFormEncoded) { implicit request =>
    val key = request.headers.get("X-Pusher-Key").get
    val signature = request.headers.get("X-Pusher-Signature").get
    val pusherRequest = Json.toJson(request.body).toString.parseJson.convertTo[AuthRequest]

    (pusherActor ask ValidateSignatureMessage(key, signature, request.body.toString)).flatMap {
      case ResponseMessage(true) =>
        (pusherActor ask AuthenticateMessage(
          pusherRequest.channelName,
          pusherRequest.socketId,
          Some(PusherModels.ChannelData(userId = "dtaniwaki", userInfo = Some(Map("user_name" -> "dtaniwaki", "name" -> "Daisuke Taniwaki").toJson)))
        )).map {
          case ResponseMessage(res: PusherModels.AuthenticatedParams) =>
            Ok(Json.toJson(res.toJson.toString))
        }
      case ResponseMessage(false) =>
        Future(Unauthorized("{}"))
      case _ =>
        throw new RuntimeException("Unknown response from pusher")
    }
  }

  def webhookAction = Action.async(parse.text) { implicit request =>
    request.body.toString.parseJson.convertTo[WebhookRequest].events foreach {
      case event =>
        logger.warn(s"Got event: $event")
    }
    Future(Ok(Json.toJson("{}")))
  }

  def triggerAction = Action.async { implicit request =>
    val (channel, event, body) = triggerForm.get
    (pusherActor ask TriggerMessage(channel, event, body.toJson)).map { case ResponseMessage(res: PusherModels.Result) => Ok(Json.toJson(res.toJson.toString)) }
  }

  def channelAction = Action.async { implicit request =>
    val (channel) = channelForm.get
    (pusherActor ask ChannelMessage(channel, Some(Seq("user_count")))).map { case ResponseMessage(res: PusherModels.Channel) => Ok(Json.toJson(res.toJson.toString)) }
  }

  def channelsAction = Action.async { implicit request =>
    val (prefix) = channelsForm.get
    (pusherActor ask ChannelsMessage(prefix, Some(Seq("user_count")))).map {
      case ResponseMessage(res: Map[_, _]) if res.forall{ case (k, v) => k.isInstanceOf[String] && v.isInstanceOf[PusherModels.Channel] } =>
        res.asInstanceOf[Map[String, PusherModels.Channel]]
    }.map { res => Ok(Json.toJson(res.toJson.toString)) }
  }

  def usersAction = Action.async { implicit request =>
    val (channel) = usersForm.get
    (pusherActor ask UsersMessage(channel)).map {
      case ResponseMessage(res: List[_]) if res.forall(_.isInstanceOf[PusherModels.User]) =>
        res.asInstanceOf[List[PusherModels.User]]
    }.map { res => Ok(Json.toJson(res.toJson.toString)) }
  }
}

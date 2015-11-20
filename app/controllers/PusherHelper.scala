package controllers

import com.github.dtaniwaki.akka_pusher.PusherRequests.{WebhookRequest, AuthRequest}
import org.slf4j.LoggerFactory
import play.api.data._
import play.api.data.Forms._

trait PusherHelper {
  protected lazy val logger = LoggerFactory.getLogger(getClass)

  protected val authForm = Form(mapping("socket_id" -> text, "channel_name" -> text)(AuthRequest.apply)(AuthRequest.unapply))
  protected val triggerForm = Form(tuple("channel" -> text, "event" -> text, "body" -> text))
  protected val channelForm = Form("channel" -> text)
  protected val channelsForm = Form("prefix" -> text)
  protected val usersForm = Form("channel" -> text)
}
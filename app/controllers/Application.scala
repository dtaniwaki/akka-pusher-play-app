package controllers

import play.api._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok("OK")
  }

  def pusher = Action {
    Ok(views.html.Application.pusher(Play.current.configuration.getString("pusher.key").get.trim))
  }

}

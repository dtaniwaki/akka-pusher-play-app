package controllers

import play.api._
import play.api.mvc._

class RootController extends Controller {

  def index = Action {
    Ok("OK")
  }

}

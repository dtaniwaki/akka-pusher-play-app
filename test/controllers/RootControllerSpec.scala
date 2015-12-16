package controllers

import play.api.mvc._
import play.api.test._
import scala.concurrent.Future

object RootControllerSpec extends PlaySpecification with Results {

  "RootController#index" should {
    "should be valid" in {
      val controller = new RootController()
      val result = controller.index().apply(FakeRequest())
      val bodyText = contentAsString(result)
      bodyText must be equalTo "OK"
    }
  }
}

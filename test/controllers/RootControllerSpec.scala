package controllers

import play.api.mvc._
import play.api.test._
import scala.concurrent.Future

object RootControllerSpec extends PlaySpecification with Results {

  "RootController#index" should {
    "should be valid" in {
      val controller = new RootController()
      val result: Future[Result] = controller.index().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText must be equalTo "OK"
    }
  }
}

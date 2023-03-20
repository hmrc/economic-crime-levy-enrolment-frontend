package uk.gov.hmrc.economiccrimelevyenrolment.behaviours

import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyenrolment.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes

import scala.concurrent.Future

trait AuthorisedBehaviour {
  self: ISpecBase =>

  def authorisedActionRoute(call: Call): Unit =
    "authorisedAction" should {
      "go to the not enrolled page if the user has insufficient enrolments" in {
        stubInsufficientEnrolments()

        val result: Future[Result] = callRoute(FakeRequest(call))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).value shouldBe routes.NotableErrorController.notRegistered().url
      }

      "go to the agent not supported page if the user has an unsupported affinity group" in {
        stubUnsupportedAffinityGroup()

        val result: Future[Result] = callRoute(FakeRequest(call))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).value shouldBe routes.NotableErrorController.agentCannotClaimEnrolment().url
      }
    }

}

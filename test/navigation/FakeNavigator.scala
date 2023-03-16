package navigation

import play.api.mvc.Call
import pages._
import uk.gov.hmrc.economiccrimelevyenrolment.models.{Mode, UserAnswers}
import uk.gov.hmrc.economiccrimelevyenrolment.pages.Page

class FakeNavigator(desiredRoute: Call) extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    desiredRoute
}

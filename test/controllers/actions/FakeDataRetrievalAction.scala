package controllers.actions

import uk.gov.hmrc.economiccrimelevyenrolment.controllers.actions.DataRetrievalAction
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.OptionalDataRequest

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction(dataToReturn: Option[UserAnswers]) extends DataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    Future(OptionalDataRequest(request.request, request.userId, dataToReturn))

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

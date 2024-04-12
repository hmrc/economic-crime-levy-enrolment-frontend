/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.economiccrimelevyenrolment.controllers.actions

import org.mockito.ArgumentMatchers
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.{AuthorisedRequest, DataRequest}
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository

import scala.concurrent.Future

class DataRetrievalOrErrorActionSpec extends SpecBase {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  class TestDataRetrievalOrErrorAction extends UserAnswersDataRetrievalOrErrorAction(mockSessionRepository) {
    override def refine[A](request: AuthorisedRequest[A]): Future[Either[Result, DataRequest[A]]] =
      super.refine(request)
  }

  val dataRetrievalOrErrorAction =
    new TestDataRetrievalOrErrorAction

  val testAction: Request[_] => Future[Result] = { _ =>
    Future(Ok("Test"))
  }

  "refine" should {
    "refine an AuthorisedRequest into a DataRequest when some useranswers is returned from session repository" in forAll {
      (
        internalId: String,
        groupId: String,
        userAnswers: UserAnswers,
        eclRegistrationReference: String,
        credentials: Credentials
      ) =>
        when(mockSessionRepository.get(ArgumentMatchers.eq(internalId))).thenReturn(Future(Some(userAnswers)))

        val result: Future[Either[Result, DataRequest[AnyContentAsEmpty.type]]] =
          dataRetrievalOrErrorAction.refine(
            AuthorisedRequest(fakeRequest, internalId, groupId, Some(eclRegistrationReference), credentials)
          )

        await(result) shouldBe Right(DataRequest(fakeRequest, internalId, groupId, credentials, userAnswers))
    }

    "refine an AuthorisedRequest into a DataRequest when none is returned from session repository" in forAll {
      (
        internalId: String,
        groupId: String,
        eclRegistrationReference: String,
        credentials: Credentials
      ) =>
        when(mockSessionRepository.get(ArgumentMatchers.eq(internalId))).thenReturn(Future(None))

        val result: Future[Either[Result, DataRequest[AnyContentAsEmpty.type]]] =
          dataRetrievalOrErrorAction.refine(
            AuthorisedRequest(fakeRequest, internalId, groupId, Some(eclRegistrationReference), credentials)
          )

        await(result) shouldBe Left(Redirect(routes.NotableErrorController.eclAlreadyAdded()))
    }
  }
}

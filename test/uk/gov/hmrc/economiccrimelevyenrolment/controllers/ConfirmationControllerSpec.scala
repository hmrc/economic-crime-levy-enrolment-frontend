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

package uk.gov.hmrc.economiccrimelevyenrolment.controllers

import org.mockito.ArgumentMatchers.any
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.models.TriState._
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository
import uk.gov.hmrc.economiccrimelevyenrolment.views.html.{AnswersAreInvalidView, ConfirmationView}
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.Future

class ConfirmationControllerSpec extends SpecBase {

  val view: ConfirmationView                       = app.injector.instanceOf[ConfirmationView]
  val answersAreInvalidView: AnswersAreInvalidView = app.injector.instanceOf[AnswersAreInvalidView]
  val mockSessionRepository: SessionRepository     = mock[SessionRepository]

  class TestContext(userAnswers: UserAnswers, groupId: String, providerId: String) {
    val controller = new ConfirmationController(
      mockSessionRepository,
      mcc,
      fakeAuthorisedActionWithoutEnrolmentCheck(userAnswers.internalId, groupId, providerId),
      view,
      answersAreInvalidView
    )
  }

  "onPageLoad" should {
    "return OK and answer invalid view" in { (userAnswers: UserAnswers) =>
      new TestContext(userAnswers.copy(hasEclReference = Some(Yes)), testGroupId, testProviderId) {

        when(mockSessionRepository.clear(userAnswers.internalId))
          .thenReturn(Future.failed(UpstreamErrorResponse(any(), any(), any(), any())))

        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe answersAreInvalidView()(fakeRequest, messages).toString
      }
    }

    "return OK and answer confirmation view" in { (userAnswers: UserAnswers) =>
      new TestContext(userAnswers.copy(hasEclReference = Some(Yes)), testGroupId, testProviderId) {

        when(mockSessionRepository.clear(userAnswers.internalId))
          .thenReturn(Future.successful(true))

        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe view()(fakeRequest, messages).toString
      }
    }
  }

}

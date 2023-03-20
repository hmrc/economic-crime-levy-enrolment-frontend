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

import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.views.html._

import scala.concurrent.Future

class NotableErrorControllerSpec extends SpecBase {

  val answersAreInvalidView: AnswersAreInvalidView                 = app.injector.instanceOf[AnswersAreInvalidView]
  val notRegisteredView: NotRegisteredView                         = app.injector.instanceOf[NotRegisteredView]
  val agentCannotClaimEnrolmentView: AgentCannotClaimEnrolmentView =
    app.injector.instanceOf[AgentCannotClaimEnrolmentView]

  class TestContext(userAnswers: UserAnswers) {
    val controller = new NotableErrorController(
      mcc,
      fakeAuthorisedAction(userAnswers.internalId),
      fakeDataRetrievalAction(userAnswers),
      appConfig,
      answersAreInvalidView,
      notRegisteredView,
      agentCannotClaimEnrolmentView
    )
  }

  "answerAreInvalid" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers) {
        val result: Future[Result] = controller.answersAreInvalid()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe answersAreInvalidView()(fakeRequest, messages).toString
      }
    }
  }

  "notRegistered" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers) {
        val result: Future[Result] = controller.notRegistered()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe notRegisteredView()(fakeRequest, messages).toString
      }
    }
  }

  "agentCannotClaimEnrolment" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers) {
        val result: Future[Result] = controller.agentCannotClaimEnrolment()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe agentCannotClaimEnrolmentView()(fakeRequest, messages).toString
      }
    }
  }

}

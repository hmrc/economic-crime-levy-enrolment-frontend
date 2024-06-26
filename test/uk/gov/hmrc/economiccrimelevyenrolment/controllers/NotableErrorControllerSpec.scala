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
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.EclEnrolment
import uk.gov.hmrc.economiccrimelevyenrolment.views.html._

import scala.concurrent.Future

class NotableErrorControllerSpec extends SpecBase {

  val answersAreInvalidView: AnswersAreInvalidView             = app.injector.instanceOf[AnswersAreInvalidView]
  val detailsDoNotMatchView: DetailsDoNotMatchView             = app.injector.instanceOf[DetailsDoNotMatchView]
  val userAlreadyEnrolledView: UserAlreadyEnrolledView         = app.injector.instanceOf[UserAlreadyEnrolledView]
  val groupAlreadyEnrolledView: GroupAlreadyEnrolledView       = app.injector.instanceOf[GroupAlreadyEnrolledView]
  val agentCannotRegisterView: AgentCannotRegisterView         = app.injector.instanceOf[AgentCannotRegisterView]
  val assistantCannotRegisterView: AssistantCannotRegisterView = app.injector.instanceOf[AssistantCannotRegisterView]
  val duplicateEnrolmentView: DuplicateEnrolmentView           = app.injector.instanceOf[DuplicateEnrolmentView]
  val eclAlreadyAddedView: EclAlreadyAddedView                 = app.injector.instanceOf[EclAlreadyAddedView]

  class TestContext(
    userAnswers: UserAnswers,
    groupId: String,
    providerId: String,
    eclRegistrationReference: Option[String] = None
  ) {
    val controller = new NotableErrorController(
      mcc,
      fakeAuthorisedActionWithoutEnrolmentCheck(userAnswers.internalId, groupId, providerId, eclRegistrationReference),
      fakeAuthorisedActionWithEnrolmentCheck(userAnswers.internalId, groupId, providerId),
      fakeAuthorisedActionAgentsAllowed,
      fakeAuthorisedActionAssistantsAllowed,
      fakeDataRetrievalAction(userAnswers),
      appConfig,
      userAlreadyEnrolledView,
      groupAlreadyEnrolledView,
      answersAreInvalidView,
      detailsDoNotMatchView,
      agentCannotRegisterView,
      assistantCannotRegisterView,
      duplicateEnrolmentView,
      eclAlreadyAddedView
    )
  }

  "answerAreInvalid" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result] = controller.answersAreInvalid()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe answersAreInvalidView()(fakeRequest, messages).toString
      }
    }
  }

  "detailsDoNotMatch" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result] = controller.detailsDoNotMatch()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe detailsDoNotMatchView()(fakeRequest, messages).toString
      }
    }
  }

  "userAlreadyEnrolled" should {
    "return OK and the correct view" in forAll { (userAnswers: UserAnswers, eclRegistrationReference: String) =>
      new TestContext(userAnswers, testGroupId, testProviderId, Some(eclRegistrationReference)) {
        val result: Future[Result] = controller.userAlreadyEnrolled()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe userAlreadyEnrolledView(eclRegistrationReference)(
          fakeRequest,
          messages
        ).toString
      }
    }
  }

  "groupAlreadyEnrolled" should {
    "return OK and the correct view" in forAll { (userAnswers: UserAnswers, eclRegistrationReference: String) =>
      new TestContext(userAnswers, testGroupId, testProviderId, Some(eclRegistrationReference)) {
        val result: Future[Result]    = controller.groupAlreadyEnrolled()(fakeRequest)
        val taxAndSchemeManagementUrl =
          s"${appConfig.taxAndSchemeManagementUrl}/services/${EclEnrolment.serviceName}/${EclEnrolment.identifierKey}~$eclRegistrationReference/users"

        status(result) shouldBe OK

        contentAsString(result) shouldBe groupAlreadyEnrolledView(eclRegistrationReference, taxAndSchemeManagementUrl)(
          fakeRequest,
          messages
        ).toString
      }
    }
  }

  "agentCannotRegister" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result] = controller.agentCannotRegister()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe agentCannotRegisterView()(fakeRequest, messages).toString
      }
    }
  }

  "assistantCannotRegister" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result] = controller.assistantCannotRegister()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe assistantCannotRegisterView()(fakeRequest, messages).toString
      }
    }
  }

  "duplicateEnrolment" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result] = controller.duplicateEnrolment()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe duplicateEnrolmentView()(fakeRequest, messages).toString
      }
    }
  }

  "eclAlreadyAddedView" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result] = controller.eclAlreadyAdded()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe eclAlreadyAddedView()(fakeRequest, messages).toString
      }
    }
  }

}

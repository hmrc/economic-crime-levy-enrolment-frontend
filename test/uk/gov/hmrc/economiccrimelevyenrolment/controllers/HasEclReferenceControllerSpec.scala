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

import org.mockito.ArgumentMatchers
import play.api.data.Form
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.forms.HasEclReferenceFormProvider
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.{TriState, UserAnswers}
import uk.gov.hmrc.economiccrimelevyenrolment.navigation.HasEclReferencePageNavigator
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository
import uk.gov.hmrc.economiccrimelevyenrolment.views.html.HasEclReferenceView

import scala.concurrent.Future

class HasEclReferenceControllerSpec extends SpecBase {

  val view: HasEclReferenceView                   = app.injector.instanceOf[HasEclReferenceView]
  val formProvider: HasEclReferenceFormProvider   = new HasEclReferenceFormProvider()
  val form: Form[TriState]                        = formProvider()
  val pageNavigator: HasEclReferencePageNavigator = new HasEclReferencePageNavigator() {
    override protected def navigateInNormalMode(userAnswers: UserAnswers): Call = onwardRoute
  }
  val mockSessionRepository: SessionRepository    = mock[SessionRepository]

  class TestContext(userAnswers: UserAnswers, groupId: String, providerId: String) {
    val controller = new HasEclReferenceController(
      mcc,
      fakeAuthorisedActionWithEnrolmentCheck(userAnswers.internalId, groupId, providerId),
      fakeDataRetrievalAction(userAnswers),
      mockSessionRepository,
      formProvider,
      pageNavigator,
      view
    )
  }

  "onPageLoad" should {
    "return OK and the correct view when no answer has already been provided" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers.copy(hasEclReference = None), testGroupId, testProviderId) {
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe view(form)(fakeRequest, messages).toString
      }
    }

    "populate the view correctly when the question has previously been answered" in forAll {
      (userAnswers: UserAnswers, hasEclReference: TriState) =>
        new TestContext(userAnswers.copy(hasEclReference = Some(hasEclReference)), testGroupId, testProviderId) {
          val result: Future[Result] = controller.onPageLoad()(fakeRequest)

          status(result) shouldBe OK

          contentAsString(result) shouldBe view(form.fill(hasEclReference))(fakeRequest, messages).toString
        }
    }
  }

  "onSubmit" should {
    "save the selected option then redirect to the next page" in forAll {
      (userAnswers: UserAnswers, hasEclReference: TriState) =>
        new TestContext(userAnswers, testGroupId, testProviderId) {
          val updatedAnswers: UserAnswers = userAnswers.copy(
            hasEclReference = Some(hasEclReference)
          )

          when(mockSessionRepository.upsert(ArgumentMatchers.eq(updatedAnswers)))
            .thenReturn(Future.successful(true))

          val result: Future[Result] =
            controller.onSubmit()(fakeRequest.withFormUrlEncodedBody(("value", hasEclReference.toString)))

          status(result) shouldBe SEE_OTHER

          redirectLocation(result) shouldBe Some(onwardRoute.url)
        }
    }

    "return a Bad Request with form errors when invalid data is submitted" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result]         = controller.onSubmit()(fakeRequest.withFormUrlEncodedBody(("value", "")))
        val formWithErrors: Form[TriState] = form.bind(Map("value" -> ""))

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) shouldBe view(formWithErrors)(fakeRequest, messages).toString
      }
    }
  }

}

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
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.connectors.EnrolmentStoreProxyConnector
import uk.gov.hmrc.economiccrimelevyenrolment.forms.EclReferenceFormProvider
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.DataRequest
import uk.gov.hmrc.economiccrimelevyenrolment.navigation.EclReferencePageNavigator
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository
import uk.gov.hmrc.economiccrimelevyenrolment.views.html.EclReferenceView
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.Future

class EclReferenceControllerSpec extends SpecBase {

  val view: EclReferenceView                                         = app.injector.instanceOf[EclReferenceView]
  val formProvider: EclReferenceFormProvider                         = new EclReferenceFormProvider()
  val form: Form[String]                                             = formProvider()
  val mockSessionRepository: SessionRepository                       = mock[SessionRepository]
  val mockEnrolmentStoreProxyConnector: EnrolmentStoreProxyConnector = mock[EnrolmentStoreProxyConnector]
  val mockAuditConnector: AuditConnector                             = mock[AuditConnector]

  val pageNavigator: EclReferencePageNavigator =
    new EclReferencePageNavigator(mockEnrolmentStoreProxyConnector, mockAuditConnector) {
      override protected def navigate(userAnswers: UserAnswers)(implicit request: DataRequest[_]): Future[Call] =
        Future.successful(onwardRoute)
    }

  class TestContext(userAnswers: UserAnswers, groupId: String, providerId: String) {
    val controller = new EclReferenceController(
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
      new TestContext(userAnswers.copy(eclReferenceNumber = None), testGroupId, testProviderId) {
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe view(form)(fakeRequest, messages).toString
      }
    }

    "populate the view correctly when the question has previously been answered" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String) =>
        new TestContext(userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber)), testGroupId, testProviderId) {
          val result: Future[Result] = controller.onPageLoad()(fakeRequest)

          status(result) shouldBe OK

          contentAsString(result) shouldBe view(form.fill(eclReferenceNumber))(fakeRequest, messages).toString
        }
    }
  }

  "onSubmit" should {
    "save the selected answer then redirect to the next page" in forAll(
      Arbitrary.arbitrary[UserAnswers],
      eclRegistrationReference
    ) { (userAnswers: UserAnswers, eclReferenceNumber: String) =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val updatedAnswers: UserAnswers = userAnswers.copy(
          eclReferenceNumber = Some(eclReferenceNumber)
        )

        when(mockSessionRepository.upsert(ArgumentMatchers.eq(updatedAnswers)))
          .thenReturn(Future.successful(true))

        val result: Future[Result] =
          controller.onSubmit()(fakeRequest.withFormUrlEncodedBody(("value", eclReferenceNumber)))

        status(result) shouldBe SEE_OTHER

        redirectLocation(result) shouldBe Some(onwardRoute.url)
      }
    }

    "return a Bad Request with form errors when invalid data is submitted" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result]       = controller.onSubmit()(fakeRequest.withFormUrlEncodedBody(("value", "")))
        val formWithErrors: Form[String] = form.bind(Map("value" -> ""))

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) shouldBe view(formWithErrors)(fakeRequest, messages).toString
      }
    }
  }

}

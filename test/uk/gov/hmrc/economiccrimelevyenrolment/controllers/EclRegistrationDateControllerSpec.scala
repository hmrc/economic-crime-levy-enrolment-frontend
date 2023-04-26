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
import uk.gov.hmrc.economiccrimelevyenrolment.connectors.{EnrolmentStoreProxyConnector, TaxEnrolmentsConnector}
import uk.gov.hmrc.economiccrimelevyenrolment.forms.EclRegistrationDateFormProvider
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.DataRequest
import uk.gov.hmrc.economiccrimelevyenrolment.navigation.EclRegistrationDatePageNavigator
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository
import uk.gov.hmrc.economiccrimelevyenrolment.views.html.EclRegistrationDateView
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDate
import scala.concurrent.Future

class EclRegistrationDateControllerSpec extends SpecBase {

  val view: EclRegistrationDateView                                  = app.injector.instanceOf[EclRegistrationDateView]
  val formProvider: EclRegistrationDateFormProvider                  = new EclRegistrationDateFormProvider()
  val form: Form[LocalDate]                                          = formProvider()
  val mockSessionRepository: SessionRepository                       = mock[SessionRepository]
  val mockEnrolmentStoreProxyConnector: EnrolmentStoreProxyConnector = mock[EnrolmentStoreProxyConnector]
  val mockTaxEnrolmentsConnector: TaxEnrolmentsConnector             = mock[TaxEnrolmentsConnector]
  val mockAuditConnector: AuditConnector                             = mock[AuditConnector]

  val pageNavigator: EclRegistrationDatePageNavigator = new EclRegistrationDatePageNavigator(
    mockEnrolmentStoreProxyConnector,
    mockTaxEnrolmentsConnector,
    mockAuditConnector
  ) {
    override protected def navigate(userAnswers: UserAnswers)(implicit request: DataRequest[_]): Future[Call] =
      Future.successful(onwardRoute)
  }

  class TestContext(userAnswers: UserAnswers, groupId: String, providerId: String) {
    val controller = new EclRegistrationDateController(
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
      new TestContext(userAnswers.copy(eclRegistrationDate = None), testGroupId, testProviderId) {
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe view(form)(fakeRequest, messages).toString
      }
    }

    "populate the view correctly when the question has previously been answered" in forAll {
      (userAnswers: UserAnswers, eclRegistrationDate: LocalDate) =>
        new TestContext(
          userAnswers.copy(eclRegistrationDate = Some(eclRegistrationDate)),
          testGroupId,
          testProviderId
        ) {
          val result: Future[Result] = controller.onPageLoad()(fakeRequest)

          status(result) shouldBe OK

          contentAsString(result) shouldBe view(form.fill(eclRegistrationDate))(fakeRequest, messages).toString
        }
    }
  }

  "onSubmit" should {
    "save the selected answer then redirect to the next page" in forAll {
      (userAnswers: UserAnswers, eclRegistrationDate: LocalDate) =>
        new TestContext(userAnswers, testGroupId, testProviderId) {
          val updatedAnswers: UserAnswers = userAnswers.copy(
            eclRegistrationDate = Some(eclRegistrationDate)
          )

          when(mockSessionRepository.upsert(ArgumentMatchers.eq(updatedAnswers)))
            .thenReturn(Future.successful(true))

          val result: Future[Result] =
            controller.onSubmit()(
              fakeRequest.withFormUrlEncodedBody(
                ("value.day", eclRegistrationDate.getDayOfMonth.toString),
                ("value.month", eclRegistrationDate.getMonthValue.toString),
                ("value.year", eclRegistrationDate.getYear.toString)
              )
            )

          status(result) shouldBe SEE_OTHER

          redirectLocation(result) shouldBe Some(onwardRoute.url)
        }
    }

    "return a Bad Request with form errors when invalid data is submitted" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers, testGroupId, testProviderId) {
        val result: Future[Result]          = controller.onSubmit()(fakeRequest.withFormUrlEncodedBody(("value", "")))
        val formWithErrors: Form[LocalDate] = form.bind(Map("value" -> ""))

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) shouldBe view(formWithErrors)(fakeRequest, messages).toString
      }
    }
  }

}

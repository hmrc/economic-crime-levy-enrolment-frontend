/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevyenrolment

import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyenrolment.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.behaviours.AuthorisedBehaviour
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.models.TriState.Yes
import uk.gov.hmrc.economiccrimelevyenrolment.models.{KeyValue, UserAnswers}
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{AllocateEnrolmentRequest, EclEnrolment}

class EclRegistrationDateISpec extends ISpecBase with AuthorisedBehaviour {

  s"GET ${routes.EclRegistrationDateController.onPageLoad().url}" should {
    behave like authorisedActionWithEnrolmentCheckRoute(routes.EclRegistrationDateController.onPageLoad())

    "respond with 200 status and the ECL registration date HTML view" in {
      stubAuthorised()

      val userAnswers =
        UserAnswers
          .empty(testInternalId)
          .copy(
            hasEclReference = Some(Yes),
            eclReferenceNumber = Some(testEclRegistrationReference),
            eclRegistrationDate = None
          )

      stubUpsert(sessionRepository, userAnswers)

      val result = callRoute(FakeRequest(routes.EclRegistrationDateController.onPageLoad()))

      status(result) shouldBe OK

      html(result) should include("Your ECL registration date")
    }

    "respond with 303 status and the ECL registration date HTML view" in {
      stubAuthorised()

      val result = callRoute(FakeRequest(routes.EclRegistrationDateController.onPageLoad()))

      status(result) shouldBe OK

      html(result) should include("Your ECL registration date")
    }
  }

  s"POST ${routes.EclRegistrationDateController.onSubmit().url}"  should {
    behave like authorisedActionWithEnrolmentCheckRoute(routes.EclRegistrationDateController.onSubmit())

    "save the provided ECL registration date then redirect to the details confirmed page" in {
      stubAuthorised()
      stubQueryKnownFacts(testEclRegistrationReference, testEclRegistrationDateString)
      stubAllocateEnrolment(
        AllocateEnrolmentRequest(
          userId = testProviderId,
          verifiers = Seq(KeyValue(EclEnrolment.verifierKey, testEclRegistrationDateString))
        )
      )

      val userAnswers =
        UserAnswers
          .empty(testInternalId)
          .copy(
            hasEclReference = Some(Yes),
            eclReferenceNumber = Some(testEclRegistrationReference),
            eclRegistrationDate = None
          )

      stubUpsert(sessionRepository, userAnswers)

      val result = callRoute(
        FakeRequest(routes.EclRegistrationDateController.onSubmit())
          .withFormUrlEncodedBody(
            ("value.day", testEclRegistrationDate.getDayOfMonth.toString),
            ("value.month", testEclRegistrationDate.getMonthValue.toString),
            ("value.year", testEclRegistrationDate.getYear.toString)
          )
      )

      status(result) shouldBe SEE_OTHER

      redirectLocation(result) shouldBe Some(routes.ConfirmationController.onPageLoad().url)
    }
  }

}

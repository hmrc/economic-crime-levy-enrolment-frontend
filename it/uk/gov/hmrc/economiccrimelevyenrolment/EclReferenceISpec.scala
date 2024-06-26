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
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers

class EclReferenceISpec extends ISpecBase with AuthorisedBehaviour {

  s"GET ${routes.EclReferenceController.onPageLoad().url}" should {
    behave like authorisedActionWithEnrolmentCheckRoute(routes.EclReferenceController.onPageLoad())

    "respond with 200 status and the ECL reference number HTML view" in {
      stubAuthorised()

      val result = callRoute(FakeRequest(routes.EclReferenceController.onPageLoad()))

      status(result) shouldBe OK

      html(result) should include("Your Economic Crime Levy (ECL) reference number")
    }
  }

  s"POST ${routes.EclReferenceController.onSubmit().url}"  should {
    behave like authorisedActionWithEnrolmentCheckRoute(routes.EclReferenceController.onSubmit())

    "save the provided ECL reference number then redirect to the ECL registration date page" in {
      val uppercaseTestEclRegistrationReference = testEclRegistrationReference.toUpperCase()

      stubAuthorised()
      stubGroupsWithEnrolment(uppercaseTestEclRegistrationReference)
      stubQueryKnownFacts(uppercaseTestEclRegistrationReference)

      val userAnswers = UserAnswers
        .empty(testInternalId)
        .copy(
          hasEclReference = Some(Yes),
          eclReferenceNumber = None,
          eclRegistrationDate = None
        )
      stubUpsert(sessionRepository, userAnswers)

      val result = callRoute(
        FakeRequest(routes.EclReferenceController.onSubmit())
          .withFormUrlEncodedBody(("value", testEclRegistrationReference))
      )

      status(result) shouldBe SEE_OTHER

      redirectLocation(result) shouldBe Some(routes.EclRegistrationDateController.onPageLoad().url)
    }
  }

}

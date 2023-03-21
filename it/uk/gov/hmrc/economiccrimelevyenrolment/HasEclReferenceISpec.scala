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

class HasEclReferenceISpec extends ISpecBase with AuthorisedBehaviour {

  s"GET ${routes.HasEclReferenceController.onPageLoad().url}" should {
    behave like authorisedActionWithEnrolmentCheckRoute(routes.HasEclReferenceController.onPageLoad())

    "respond with 200 status and the do you have an ECL reference number HTML view" in {
      stubAuthorised()

      val result = callRoute(FakeRequest(routes.HasEclReferenceController.onPageLoad()))

      status(result) shouldBe OK

      html(result) should include("Do you have an Economic Crime Levy (ECL) reference number?")
    }
  }

  s"POST ${routes.HasEclReferenceController.onSubmit().url}"  should {
    behave like authorisedActionWithEnrolmentCheckRoute(routes.HasEclReferenceController.onSubmit())

    "save the selected ECL reference option then redirect to the ECL reference number page when the answer is 'Yes'" in {
      stubAuthorised()

      val result = callRoute(
        FakeRequest(routes.HasEclReferenceController.onSubmit())
          .withFormUrlEncodedBody(("value", "Yes"))
      )

      status(result) shouldBe SEE_OTHER

      redirectLocation(result) shouldBe Some(routes.EclReferenceController.onPageLoad().url)
    }

    "save the selected ECL reference option then redirect to the need to register for ECL page when the answer is 'No'" in {
      stubAuthorised()

      val result = callRoute(
        FakeRequest(routes.HasEclReferenceController.onSubmit())
          .withFormUrlEncodedBody(("value", "No"))
      )

      status(result) shouldBe SEE_OTHER

      redirectLocation(result) shouldBe Some(routes.RegistrationController.onPageLoad().url)
    }

    "save the selected ECL reference option then redirect to the find your ECL reference page when the answer is 'Unknown'" in {
      stubAuthorised()

      val result = callRoute(
        FakeRequest(routes.HasEclReferenceController.onSubmit())
          .withFormUrlEncodedBody(("value", "Unknown"))
      )

      status(result) shouldBe SEE_OTHER

      redirectLocation(result) shouldBe Some(routes.FindEclReferenceController.onPageLoad().url)
    }
  }

}

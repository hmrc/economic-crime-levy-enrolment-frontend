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

class ConfirmationISpec extends ISpecBase with AuthorisedBehaviour {

  s"GET ${routes.ConfirmationController.onPageLoad().url}" should {
    behave like authorisedActionWithoutEnrolmentCheckRoute(routes.ConfirmationController.onPageLoad())

    "respond with 200 status and the details confirmed HTML view" in {
      stubAuthorisedWithEclEnrolment()

      val result = callRoute(FakeRequest(routes.ConfirmationController.onPageLoad()))

      status(result) shouldBe OK

      html(result) should include("Your details have been confirmed")
    }
  }

}

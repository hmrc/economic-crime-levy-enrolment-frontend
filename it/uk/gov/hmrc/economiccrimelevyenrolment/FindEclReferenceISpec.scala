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

class FindEclReferenceISpec extends ISpecBase with AuthorisedBehaviour {

  s"GET ${routes.FindEclReferenceController.onPageLoad().url}" should {
    behave like authorisedActionWithEnrolmentCheckRoute(routes.FindEclReferenceController.onPageLoad())

    "respond with 200 status and the find your ECL reference number HTML view" in {
      stubAuthorised()

      val result = callRoute(FakeRequest(routes.FindEclReferenceController.onPageLoad()))

      status(result) shouldBe OK

      html(result) should include("How to find your Economic Crime Levy reference number")
    }
  }

}

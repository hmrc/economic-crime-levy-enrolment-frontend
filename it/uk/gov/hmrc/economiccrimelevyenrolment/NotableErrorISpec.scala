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

class NotableErrorISpec extends ISpecBase with AuthorisedBehaviour {

  s"GET ${routes.NotableErrorController.answersAreInvalid().url}" should {
    behave like authorisedActionRoute(routes.NotableErrorController.answersAreInvalid())

    "respond with 200 status and the answers are invalid HTML view" in {
      stubAuthorised()

      val result = callRoute(FakeRequest(routes.NotableErrorController.answersAreInvalid()))

      status(result) shouldBe OK
      html(result) should include("The answers you provided are not valid")
    }
  }

  s"GET ${routes.NotableErrorController.notRegistered().url}" should {
    "respond with 200 status and the not registered HTML view" in {
      stubAuthorised()

      val result = callRoute(FakeRequest(routes.NotableErrorController.notRegistered()))

      status(result) shouldBe OK
      html(result) should include("You need to register for the Economic Crime Levy")
    }
  }

  s"GET ${routes.NotableErrorController.agentCannotClaimEnrolment().url}" should {
    "respond with 200 status and the agent cannot claim enrolment HTML view" in {
      stubAuthorised()

      val result = callRoute(FakeRequest(routes.NotableErrorController.agentCannotClaimEnrolment()))

      status(result) shouldBe OK
      html(result) should include("You cannot use this service to claim an Economic Crime Levy enrolment")
    }
  }

}

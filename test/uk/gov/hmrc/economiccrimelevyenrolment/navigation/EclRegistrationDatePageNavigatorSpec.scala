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

package uk.gov.hmrc.economiccrimelevyenrolment.navigation

import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.connectors.EnrolmentStoreProxyConnector
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.{NormalMode, UserAnswers}

class EclRegistrationDatePageNavigatorSpec extends SpecBase {

  val mockEnrolmentStoreProxyConnector: EnrolmentStoreProxyConnector = mock[EnrolmentStoreProxyConnector]
  val pageNavigator                                                  = new EclRegistrationDatePageNavigator(mockEnrolmentStoreProxyConnector)

  "nextPage" should {
    "return a Call to the confirmation page in NormalMode when the date of registration matches" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String) =>
        val updatedAnswers: UserAnswers = userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber))

      // TODO: Add route when next page routing logic
    }

    "return a Call to the details do not match page in NormalMode when the date of registration does not match" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String) =>
        val updatedAnswers: UserAnswers = userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber))

      // TODO: Add route when next page routing logic
    }

    "return a Call to the answers are invalid page in NormalMode when no answer has been provided" in forAll {
      userAnswers: UserAnswers =>
        val updatedAnswers: UserAnswers = userAnswers.copy(eclRegistrationDate = None)

        await(pageNavigator.nextPage(NormalMode, updatedAnswers)(fakeRequest)) shouldBe
          routes.NotableErrorController.answersAreInvalid()
    }
  }

}

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
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.TriState._
import uk.gov.hmrc.economiccrimelevyenrolment.models.{NormalMode, UserAnswers}

class HasEclReferencePageNavigatorSpec extends SpecBase {

  val pageNavigator = new HasEclReferencePageNavigator()

  "nextPage" should {
    "return a Call to the you need to register for ECL page in NormalMode when the answer is 'No'" in forAll {
      userAnswers: UserAnswers =>
        val updatedAnswers: UserAnswers = userAnswers.copy(hasEclReference = Some(No))

        pageNavigator.nextPage(NormalMode, updatedAnswers) shouldBe routes.RegistrationController.onPageLoad()
    }
  }

}

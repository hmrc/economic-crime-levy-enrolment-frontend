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
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.{TriState, UserAnswers}

class HasEclReferencePageNavigatorSpec extends SpecBase {

  val pageNavigator = new HasEclReferencePageNavigator()

  "nextPage" should {
    "return a Call to the ??? page in NormalMode" in forAll { (userAnswers: UserAnswers, hasEclReference: TriState) =>
      val updatedAnswers: UserAnswers = userAnswers.copy(hasEclReference = Some(hasEclReference))

    // TODO: Implement navigation test when navigation is in place
    }
  }

}

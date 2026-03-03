/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevyenrolment.generators

import io.github.martinhh.derived.scalacheck.deriveArbitrary
import org.scalacheck.Arbitrary
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.economiccrimelevyenrolment.EclTestData
import uk.gov.hmrc.economiccrimelevyenrolment.models._
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd._

object CachedArbitraries extends EclTestData with Generators {

  given arbGroupEnrolmentsResponse: Arbitrary[GroupEnrolmentsResponse]                   = deriveArbitrary
  given arbUserAnswers: Arbitrary[UserAnswers]                                           = deriveArbitrary
  given arbTriState: Arbitrary[TriState]                                                 = deriveArbitrary
  given arbQueryKnownFactsResponse: Arbitrary[QueryKnownFactsResponse]                   = deriveArbitrary
  given arbAllocateEnrolmentRequest: Arbitrary[AllocateEnrolmentRequest]                 = deriveArbitrary
  given arbCredentials: Arbitrary[Credentials]                                           = deriveArbitrary
  given arbQueryGroupsWithEnrolmentResponse: Arbitrary[QueryGroupsWithEnrolmentResponse] = deriveArbitrary
}

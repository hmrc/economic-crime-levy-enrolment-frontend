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

package uk.gov.hmrc.economiccrimelevyenrolment.generators

import org.scalacheck.Arbitrary
import org.scalacheck.derive.MkArbitrary
import uk.gov.hmrc.economiccrimelevyenrolment.EclTestData
import uk.gov.hmrc.economiccrimelevyenrolment.models._
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd._
import com.danielasfregola.randomdatagenerator.RandomDataGenerator.derivedArbitrary

object CachedArbitraries extends EclTestData with Generators {

  private def mkArb[T](implicit mkArb: MkArbitrary[T]): Arbitrary[T] = MkArbitrary[T].arbitrary

  implicit lazy val arbGroupEnrolmentsResponse: Arbitrary[GroupEnrolmentsResponse]   = mkArb
  implicit lazy val arbMode: Arbitrary[Mode]                                         = mkArb
  implicit lazy val arbUserAnswers: Arbitrary[UserAnswers]                           = mkArb
  implicit lazy val arbTriState: Arbitrary[TriState]                                 = mkArb
  implicit lazy val arbQueryKnownFactsResponse: Arbitrary[QueryKnownFactsResponse]   = mkArb
  implicit lazy val arbAllocateEnrolmentRequest: Arbitrary[AllocateEnrolmentRequest] = mkArb

}

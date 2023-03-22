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

package uk.gov.hmrc.economiccrimelevyenrolment

import com.danielasfregola.randomdatagenerator.RandomDataGenerator.derivedArbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.{AffinityGroup, EnrolmentIdentifier, Enrolments, Enrolment => AuthEnrolment}
import uk.gov.hmrc.economiccrimelevyenrolment.generators.Generators
import uk.gov.hmrc.economiccrimelevyenrolment.models._
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{EclEnrolment, Enrolment, GroupEnrolmentsResponse}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}

final case class EnrolmentsWithEcl(enrolments: Enrolments, eclReferenceNumber: String)

final case class EnrolmentsWithoutEcl(enrolments: Enrolments)

final case class GroupEnrolmentsResponseWithEcl(
  groupEnrolmentsResponse: GroupEnrolmentsResponse,
  eclReferenceNumber: String
)

final case class GroupEnrolmentsResponseWithoutEcl(groupEnrolmentsResponse: GroupEnrolmentsResponse)

trait EclTestData { self: Generators =>

  implicit val arbInstant: Arbitrary[Instant] = Arbitrary {
    Instant.now()
  }

  implicit val arbLocalDate: Arbitrary[LocalDate] = Arbitrary {
    LocalDate.now()
  }

  implicit val arbEnrolmentsWithEcl: Arbitrary[EnrolmentsWithEcl] = Arbitrary {
    for {
      enrolments         <- Arbitrary.arbitrary[Enrolments]
      enrolment          <- Arbitrary.arbitrary[AuthEnrolment]
      eclReferenceNumber <- Arbitrary.arbitrary[String]
      eclEnrolment        = enrolment.copy(
                              key = EclEnrolment.ServiceName,
                              identifiers =
                                Seq(EnrolmentIdentifier(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber))
                            )
    } yield EnrolmentsWithEcl(enrolments.copy(enrolments.enrolments + eclEnrolment), eclReferenceNumber)
  }

  implicit val arbEnrolmentsWithoutEcl: Arbitrary[EnrolmentsWithoutEcl] = Arbitrary {
    Arbitrary
      .arbitrary[Enrolments]
      .retryUntil(!_.enrolments.exists(_.key == EclEnrolment.ServiceName))
      .map(EnrolmentsWithoutEcl)
  }

  implicit val arbGroupEnrolmentsResponseWithEcl: Arbitrary[GroupEnrolmentsResponseWithEcl] = Arbitrary {
    for {
      enrolmentsWithEcl <- Arbitrary.arbitrary[EnrolmentsWithEcl]
    } yield GroupEnrolmentsResponseWithEcl(
      GroupEnrolmentsResponse(
        authEnrolmentsToEnrolments(enrolmentsWithEcl.enrolments)
      ),
      enrolmentsWithEcl.eclReferenceNumber
    )
  }

  implicit val arbGroupEnrolmentsResponseWithoutEcl: Arbitrary[GroupEnrolmentsResponseWithoutEcl] = Arbitrary {
    for {
      enrolmentsWithoutEcl <- Arbitrary.arbitrary[EnrolmentsWithoutEcl]
    } yield GroupEnrolmentsResponseWithoutEcl(
      GroupEnrolmentsResponse(
        authEnrolmentsToEnrolments(enrolmentsWithoutEcl.enrolments)
      )
    )
  }

  implicit val arbAffinityGroup: Arbitrary[AffinityGroup] = Arbitrary {
    Gen.oneOf(Seq(Organisation, Individual, Agent))
  }

  private def authEnrolmentsToEnrolments(authEnrolments: Enrolments) =
    authEnrolments.enrolments
      .map(e => Enrolment(e.key, e.identifiers.map(i => KeyValue(i.key, i.value)), Seq.empty))
      .toSeq

  def alphaNumericString: String = Gen.alphaNumStr.retryUntil(_.nonEmpty).sample.get

  val minDate: LocalDate = LocalDate.of(1970, 1, 1)
  val maxDate: LocalDate = LocalDate.of(2999, 12, 31)

  def localDate: LocalDate = datesBetween(minDate, maxDate).sample.get

  val testInternalId: String                = alphaNumericString
  val testGroupId: String                   = alphaNumericString
  val testEclRegistrationReference: String  = alphaNumericString
  val testEclRegistrationDate: LocalDate    = localDate
  val testEclRegistrationDateString: String = testEclRegistrationDate.format(DateTimeFormatter.BASIC_ISO_DATE)

}

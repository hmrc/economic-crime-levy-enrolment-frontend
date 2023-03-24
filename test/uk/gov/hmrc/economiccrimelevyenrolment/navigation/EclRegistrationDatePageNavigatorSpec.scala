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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.connectors.{EnrolmentStoreProxyConnector, TaxEnrolmentsConnector}
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{EclEnrolment, Enrolment, QueryKnownFactsResponse}
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.DataRequest
import uk.gov.hmrc.economiccrimelevyenrolment.models.{KeyValue, NormalMode, UserAnswers}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class EclRegistrationDatePageNavigatorSpec extends SpecBase {

  val mockEnrolmentStoreProxyConnector: EnrolmentStoreProxyConnector = mock[EnrolmentStoreProxyConnector]
  val mockTaxEnrolmentsConnector: TaxEnrolmentsConnector             = mock[TaxEnrolmentsConnector]
  val pageNavigator                                                  = new EclRegistrationDatePageNavigator(mockEnrolmentStoreProxyConnector, mockTaxEnrolmentsConnector)

  "nextPage" should {
    "return a Call to the confirmation page in NormalMode when the date of registration matches" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String, eclRegistrationDate: LocalDate, request: DataRequest[_]) =>
        val updatedAnswers: UserAnswers = userAnswers.copy(
          eclReferenceNumber = Some(eclReferenceNumber),
          eclRegistrationDate = Some(eclRegistrationDate)
        )

        val eclRegistrationDateString: String = eclRegistrationDate.format(DateTimeFormatter.BASIC_ISO_DATE)

        val knownFacts: Seq[KeyValue] = Seq(
          KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber),
          KeyValue(key = EclEnrolment.VerifierKey, value = eclRegistrationDateString)
        )

        val expectedResponse: QueryKnownFactsResponse = QueryKnownFactsResponse(
          service = EclEnrolment.ServiceName,
          enrolments = Seq(
            Enrolment(
              service = EclEnrolment.ServiceName,
              identifiers = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber)),
              verifiers = Seq(KeyValue(key = EclEnrolment.VerifierKey, value = eclRegistrationDateString))
            )
          )
        )

        when(mockEnrolmentStoreProxyConnector.queryKnownFacts(ArgumentMatchers.eq(knownFacts))(any()))
          .thenReturn(Future.successful(expectedResponse))

        // TODO: Do we need argument matchers on all explicit parameters?
        when(mockTaxEnrolmentsConnector.allocateEnrolment(any(), ArgumentMatchers.eq(eclReferenceNumber), any())(any()))
          .thenReturn(Future.successful(()))

        await(
          pageNavigator.nextPage(NormalMode, updatedAnswers)(request)
        ) shouldBe routes.ConfirmationController.onPageLoad()
    }

    "return a Call to the details do not match page in NormalMode when the date of registration does not match" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String, eclRegistrationDate: LocalDate, request: DataRequest[_]) =>
        val updatedAnswers: UserAnswers = userAnswers.copy(
          eclReferenceNumber = Some(eclReferenceNumber),
          eclRegistrationDate = Some(eclRegistrationDate)
        )

        val eclRegistrationDateString: String = eclRegistrationDate.format(DateTimeFormatter.BASIC_ISO_DATE)

        val knownFacts: Seq[KeyValue] = Seq(
          KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber),
          KeyValue(key = EclEnrolment.VerifierKey, value = eclRegistrationDateString)
        )

        val expectedResponse: QueryKnownFactsResponse = QueryKnownFactsResponse(
          service = EclEnrolment.ServiceName,
          enrolments = Seq(
            Enrolment(
              service = EclEnrolment.ServiceName,
              identifiers = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber)),
              verifiers = Seq(KeyValue(key = EclEnrolment.VerifierKey, value = "99991231"))
            )
          )
        )

        when(mockEnrolmentStoreProxyConnector.queryKnownFacts(ArgumentMatchers.eq(knownFacts))(any()))
          .thenReturn(Future.successful(expectedResponse))

        await(
          pageNavigator.nextPage(NormalMode, updatedAnswers)(request)
        ) shouldBe routes.NotableErrorController.detailsDoNotMatch()
    }

    "return a Call to the answers are invalid page in NormalMode when no answer has been provided" in forAll {
      (userAnswers: UserAnswers, request: DataRequest[_]) =>
        val updatedAnswers: UserAnswers = userAnswers.copy(eclRegistrationDate = None)

        await(pageNavigator.nextPage(NormalMode, updatedAnswers)(request)) shouldBe
          routes.NotableErrorController.answersAreInvalid()
    }
  }

}
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
import uk.gov.hmrc.economiccrimelevyenrolment.connectors.EnrolmentStoreProxyConnector
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{EclEnrolment, Enrolment, QueryGroupsWithEnrolmentResponse, QueryKnownFactsResponse}
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.DataRequest
import uk.gov.hmrc.economiccrimelevyenrolment.models.{KeyValue, UserAnswers}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.Future

class EclReferencePageNavigatorSpec extends SpecBase {

  val mockEnrolmentStoreProxyConnector: EnrolmentStoreProxyConnector = mock[EnrolmentStoreProxyConnector]
  val mockAuditConnector: AuditConnector                             = mock[AuditConnector]

  val pageNavigator = new EclReferencePageNavigator(mockEnrolmentStoreProxyConnector, mockAuditConnector)

  "nextPage" should {
    "return a Call to the date of registration page when the ECL reference number matches and isn't already allocated to another group" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String, request: DataRequest[_]) =>
        val updatedAnswers: UserAnswers               = userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber))
        val knownFacts: Seq[KeyValue]                 = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber))
        val expectedResponse: QueryKnownFactsResponse = QueryKnownFactsResponse(
          service = EclEnrolment.ServiceName,
          enrolments = Seq(
            Enrolment(
              identifiers = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber)),
              verifiers = Seq.empty
            )
          )
        )

        when(mockEnrolmentStoreProxyConnector.queryGroupsWithEnrolment(ArgumentMatchers.eq(eclReferenceNumber))(any()))
          .thenReturn(Future.successful(None))

        when(mockEnrolmentStoreProxyConnector.queryKnownFacts(ArgumentMatchers.eq(knownFacts))(any()))
          .thenReturn(Future.successful(Some(expectedResponse)))

        when(mockEnrolmentStoreProxyConnector.queryGroupsWithEnrolment(ArgumentMatchers.eq(eclReferenceNumber))(any()))
          .thenReturn(Future.successful(None))

        await(
          pageNavigator.nextPage(updatedAnswers)(request)
        ) shouldBe routes.EclRegistrationDateController.onPageLoad()
    }

    "return a Call to the details are invalid page when the ECL reference number does not match and isn't already allocated to another group" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String, request: DataRequest[_]) =>
        val updatedAnswers: UserAnswers               = userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber))
        val knownFacts: Seq[KeyValue]                 = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber))
        val expectedResponse: QueryKnownFactsResponse = QueryKnownFactsResponse(
          service = EclEnrolment.ServiceName,
          enrolments = Seq(
            Enrolment(
              identifiers = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = "invalid-reference")),
              verifiers = Seq.empty
            )
          )
        )

        when(mockEnrolmentStoreProxyConnector.queryGroupsWithEnrolment(ArgumentMatchers.eq(eclReferenceNumber))(any()))
          .thenReturn(Future.successful(None))

        when(mockEnrolmentStoreProxyConnector.queryKnownFacts(ArgumentMatchers.eq(knownFacts))(any()))
          .thenReturn(Future.successful(Some(expectedResponse)))

        await(
          pageNavigator.nextPage(updatedAnswers)(request)
        ) shouldBe routes.NotableErrorController.detailsDoNotMatch()

        verify(mockAuditConnector, times(1)).sendExtendedEvent(any())(any(), any())

        reset(mockAuditConnector)
    }

    "return a Call to the details are invalid page when the API does not return any results and the ECL reference isn't already allocated to another group" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String, request: DataRequest[_]) =>
        val updatedAnswers: UserAnswers = userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber))
        val knownFacts: Seq[KeyValue]   = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber))

        when(mockEnrolmentStoreProxyConnector.queryGroupsWithEnrolment(ArgumentMatchers.eq(eclReferenceNumber))(any()))
          .thenReturn(Future.successful(None))

        when(mockEnrolmentStoreProxyConnector.queryKnownFacts(ArgumentMatchers.eq(knownFacts))(any()))
          .thenReturn(Future.successful(None))

        await(
          pageNavigator.nextPage(updatedAnswers)(request)
        ) shouldBe routes.NotableErrorController.detailsDoNotMatch()

        verify(mockAuditConnector, times(1)).sendExtendedEvent(any())(any(), any())

        reset(mockAuditConnector)
    }

    "return a Call to the duplicate enrolment page when the ECL already allocated to another group" in forAll {
      (
        userAnswers: UserAnswers,
        eclReferenceNumber: String,
        groupsWithEnrolment: QueryGroupsWithEnrolmentResponse,
        request: DataRequest[_]
      ) =>
        val updatedAnswers: UserAnswers = userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber))

        when(mockEnrolmentStoreProxyConnector.queryGroupsWithEnrolment(ArgumentMatchers.eq(eclReferenceNumber))(any()))
          .thenReturn(Future.successful(Some(groupsWithEnrolment)))

        await(
          pageNavigator.nextPage(updatedAnswers)(request)
        ) shouldBe routes.NotableErrorController.duplicateEnrolment()
    }

    "return a Call to the answers are invalid page when no answer has been provided" in forAll {
      (userAnswers: UserAnswers, request: DataRequest[_]) =>
        val updatedAnswers: UserAnswers = userAnswers.copy(eclReferenceNumber = None)

        await(pageNavigator.nextPage(updatedAnswers)(request)) shouldBe
          routes.NotableErrorController.answersAreInvalid()
    }
  }

}

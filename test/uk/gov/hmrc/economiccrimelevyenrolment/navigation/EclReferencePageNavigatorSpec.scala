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
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{EclEnrolment, Enrolment, QueryKnownFactsResponse}
import uk.gov.hmrc.economiccrimelevyenrolment.models.{KeyValue, NormalMode, UserAnswers}

import scala.concurrent.Future

class EclReferencePageNavigatorSpec extends SpecBase {

  val mockEnrolmentStoreProxyConnector: EnrolmentStoreProxyConnector = mock[EnrolmentStoreProxyConnector]
  val pageNavigator                                                  = new EclReferencePageNavigator(mockEnrolmentStoreProxyConnector)

  "nextPage" should {
    "return a Call to the date of registration page in NormalMode when the ECL reference number matches" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String) =>
        val updatedAnswers: UserAnswers               = userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber))
        val knownFacts: Seq[KeyValue]                 = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber))
        val expectedResponse: QueryKnownFactsResponse = QueryKnownFactsResponse(
          service = EclEnrolment.ServiceName,
          enrolments = Seq(
            Enrolment(
              service = EclEnrolment.ServiceName,
              identifiers = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber)),
              verifiers = Seq.empty
            )
          )
        )

        when(mockEnrolmentStoreProxyConnector.queryKnownFacts(ArgumentMatchers.eq(knownFacts))(any()))
          .thenReturn(Future.successful(expectedResponse))

        await(
          pageNavigator.nextPage(NormalMode, updatedAnswers)(fakeRequest)
        ) shouldBe routes.EclRegistrationDateController.onPageLoad()
    }

    "return a Call to the details are invalid page in NormalMode when the ECL reference number does not match" in forAll {
      (userAnswers: UserAnswers, eclReferenceNumber: String) =>
        val updatedAnswers: UserAnswers               = userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber))
        val knownFacts: Seq[KeyValue]                 = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber))
        val expectedResponse: QueryKnownFactsResponse = QueryKnownFactsResponse(
          service = EclEnrolment.ServiceName,
          enrolments = Seq(
            Enrolment(
              service = EclEnrolment.ServiceName,
              identifiers = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = "invalid-reference")),
              verifiers = Seq.empty
            )
          )
        )

        when(mockEnrolmentStoreProxyConnector.queryKnownFacts(ArgumentMatchers.eq(knownFacts))(any()))
          .thenReturn(Future.successful(expectedResponse))

        await(
          pageNavigator.nextPage(NormalMode, updatedAnswers)(fakeRequest)
        ) shouldBe routes.NotableErrorController.detailsDoNotMatch()
    }

    "return a Call to the answers are invalid page in NormalMode when no answer has been provided" in forAll {
      userAnswers: UserAnswers =>
        val updatedAnswers: UserAnswers = userAnswers.copy(eclReferenceNumber = None)

        await(pageNavigator.nextPage(NormalMode, updatedAnswers)(fakeRequest)) shouldBe
          routes.NotableErrorController.answersAreInvalid()
    }
  }

}

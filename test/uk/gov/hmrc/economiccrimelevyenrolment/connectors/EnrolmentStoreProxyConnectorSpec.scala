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

package uk.gov.hmrc.economiccrimelevyenrolment.connectors

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.KeyValue
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd._
import uk.gov.hmrc.http.{HttpClient, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future
import uk.gov.hmrc.http.HttpReads.Implicits._

class EnrolmentStoreProxyConnectorSpec extends SpecBase {

  val mockHttpClient: HttpClientV2       = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val connector                          = new EnrolmentStoreProxyConnectorImpl(appConfig, mockHttpClient)
  val enrolmentStoreUrl: String          = s"${appConfig.enrolmentStoreProxyBaseUrl}/enrolment-store-proxy/enrolment-store"

  "getEnrolmentsForGroup" should {
    "return a list of enrolments for the specified group when the http client returns a list of enrolments" in forAll {
      (groupId: String, groupEnrolments: Option[GroupEnrolmentsResponse]) =>
        val expectedUrl = url"$enrolmentStoreUrl/groups/$groupId/enrolments"

        when(mockHttpClient.get(ArgumentMatchers.eq(expectedUrl))(any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.execute[Option[GroupEnrolmentsResponse]](any(), any()))
          .thenReturn(Future.successful(groupEnrolments))

        val result = await(connector.getEnrolmentsForGroup(groupId))

        result shouldBe groupEnrolments

        verify(mockHttpClient, times(1))
          .get(ArgumentMatchers.eq(expectedUrl))(any())

        reset(mockHttpClient)
    }
  }

  "queryKnownFacts" should {
    "return known facts when the http client returns known facts" in forAll {
      (
        eclRegistrationReference: String,
        eclRegistrationDate: LocalDate,
        queryKnownFactsResponse: Option[QueryKnownFactsResponse]
      ) =>
        val expectedUrl                    = url"$enrolmentStoreUrl/enrolments"
        val expectedKnownFacts             = Seq(
          KeyValue(EclEnrolment.identifierKey, eclRegistrationReference),
          KeyValue(EclEnrolment.verifierKey, eclRegistrationDate.format(DateTimeFormatter.BASIC_ISO_DATE))
        )
        val expectedQueryKnownFactsRequest = QueryKnownFactsRequest(
          service = EclEnrolment.serviceName,
          knownFacts = expectedKnownFacts
        )

        when(mockHttpClient.post(ArgumentMatchers.eq(expectedUrl))(any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.withBody(any())(any(), any(), any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.execute[Option[QueryKnownFactsResponse]](any(), any()))
          .thenReturn(Future.successful(queryKnownFactsResponse))

        val result = await(connector.queryKnownFacts(expectedKnownFacts))

        result shouldBe queryKnownFactsResponse

        verify(mockHttpClient, times(1))
          .post(ArgumentMatchers.eq(expectedUrl))

        reset(mockHttpClient)
    }
  }

  "queryGroupsWithEnrolment" should {
    "return the groups with enrolment response when the http client returns one" in forAll {
      (
        eclRegistrationReference: String,
        queryGroupsWithEnrolmentResponse: Option[QueryGroupsWithEnrolmentResponse]
      ) =>
        val expectedUrl =
          url"$enrolmentStoreUrl/enrolments/${EclEnrolment.enrolmentKey(eclRegistrationReference)}/groups?ignore-assignments=true"

        when(mockHttpClient.get(ArgumentMatchers.eq(expectedUrl))(any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.execute[Option[QueryGroupsWithEnrolmentResponse]](any(), any()))
          .thenReturn(Future.successful(queryGroupsWithEnrolmentResponse))

        val result = await(connector.queryGroupsWithEnrolment(eclRegistrationReference))

        result shouldBe queryGroupsWithEnrolmentResponse

        verify(mockHttpClient, times(1))
          .get(ArgumentMatchers.eq(expectedUrl))

        reset(mockHttpClient)
    }
  }

}

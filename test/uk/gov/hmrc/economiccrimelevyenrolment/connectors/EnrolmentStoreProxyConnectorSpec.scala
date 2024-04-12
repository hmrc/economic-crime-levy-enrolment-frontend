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
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.KeyValue
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd._
import uk.gov.hmrc.http.{HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class EnrolmentStoreProxyConnectorSpec extends SpecBase {

  val mockHttpClient: HttpClientV2       = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val connector                          = new EnrolmentStoreProxyConnectorImpl(appConfig, mockHttpClient)
  val enrolmentStoreUrl: String          = s"${appConfig.enrolmentStoreProxyBaseUrl}/enrolment-store-proxy/enrolment-store"

  "getEnrolmentsForGroup" should {
    "return a list of enrolments for the specified group when the http client returns a list of enrolments" in forAll {
      (groupId: String, groupEnrolments: GroupEnrolmentsResponse) =>
        val expectedUrl = url"$enrolmentStoreUrl/groups/$groupId/enrolments"

        when(mockHttpClient.get(ArgumentMatchers.eq(expectedUrl))(any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, Json.toJson(groupEnrolments).toString())))

        val result = await(connector.getEnrolmentsForGroup(groupId))

        result shouldBe Some(groupEnrolments)

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
        queryKnownFactsResponse: QueryKnownFactsResponse
      ) =>
        val expectedUrl        = url"$enrolmentStoreUrl/enrolments"
        val expectedKnownFacts = Seq(
          KeyValue(EclEnrolment.identifierKey, eclRegistrationReference),
          KeyValue(EclEnrolment.verifierKey, eclRegistrationDate.format(DateTimeFormatter.BASIC_ISO_DATE))
        )

        when(mockHttpClient.post(ArgumentMatchers.eq(expectedUrl))(any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.withBody(any())(any(), any(), any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, Json.toJson(queryKnownFactsResponse).toString())))

        val result = await(connector.queryKnownFacts(expectedKnownFacts))

        result shouldBe Some(queryKnownFactsResponse)

        verify(mockHttpClient, times(1))
          .post(ArgumentMatchers.eq(expectedUrl))(any())

        reset(mockHttpClient)
    }
  }

  "queryGroupsWithEnrolment" should {
    "return the groups with enrolment response when the http client returns one" in forAll {
      (
        eclRegistrationReference: String,
        queryGroupsWithEnrolmentResponse: QueryGroupsWithEnrolmentResponse
      ) =>
        val expectedUrl =
          url"$enrolmentStoreUrl/enrolments/${EclEnrolment.enrolmentKey(eclRegistrationReference)}/groups?ignore-assignments=true"

        when(mockHttpClient.get(ArgumentMatchers.eq(expectedUrl))(any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, Json.toJson(queryGroupsWithEnrolmentResponse).toString())))

        val result = await(connector.queryGroupsWithEnrolment(eclRegistrationReference))

        result shouldBe Some(queryGroupsWithEnrolmentResponse)

        verify(mockHttpClient, times(1))
          .get(ArgumentMatchers.eq(expectedUrl))(any())

        reset(mockHttpClient)
    }
  }

}

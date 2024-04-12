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
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR}
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{AllocateEnrolmentRequest, EclEnrolment}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HttpResponse, StringContextOps}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class TaxEnrolmentsConnectorSpec extends SpecBase {
  val mockHttpClient: HttpClientV2       = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val connector                          = new TaxEnrolmentsConnector(appConfig, mockHttpClient)

  "allocateEnrolment" should {
    "return unit when the http client successfully returns a http response" in forAll {
      allocateEnrolmentRequest: AllocateEnrolmentRequest =>
        reset(mockHttpClient)

        val groupId      = testGroupId
        val enrolmentKey = s"${EclEnrolment.serviceName}~${EclEnrolment.identifierKey}~$testEclRegistrationReference"

        val expectedUrl = url"${appConfig.taxEnrolmentsBaseUrl}/tax-enrolments/groups/$groupId/enrolments/$enrolmentKey"

        val response = HttpResponse(CREATED, "", Map.empty)

        when(mockHttpClient.post(ArgumentMatchers.eq(expectedUrl))(any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.withBody(any())(any(), any(), any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(response))

        val result: Unit =
          await(connector.allocateEnrolment(groupId, testEclRegistrationReference, allocateEnrolmentRequest))

        result shouldBe ()

        verify(mockHttpClient, times(1))
          .post(ArgumentMatchers.eq(expectedUrl))(any())
    }

    "throw an UpstreamErrorResponse exception when the http client fails to return a success response" in forAll {
      allocateEnrolmentRequest: AllocateEnrolmentRequest =>
        reset(mockHttpClient)

        val groupId      = testGroupId
        val enrolmentKey = s"${EclEnrolment.serviceName}~${EclEnrolment.identifierKey}~$testEclRegistrationReference"

        val expectedUrl = url"${appConfig.taxEnrolmentsBaseUrl}/tax-enrolments/groups/$groupId/enrolments/$enrolmentKey"

        when(mockHttpClient.post(ArgumentMatchers.eq(expectedUrl))(any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.withBody(any())(any(), any(), any()))
          .thenReturn(mockRequestBuilder)

        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        Try(await(connector.allocateEnrolment(groupId, testEclRegistrationReference, allocateEnrolmentRequest))) match {
          case Failure(thr) => thr.getMessage shouldBe "Internal server error"
          case Success(_)   => fail("expected exception to be thrown")
        }

        verify(mockHttpClient, times(1))
          .post(ArgumentMatchers.eq(expectedUrl))(any())
    }
  }
}

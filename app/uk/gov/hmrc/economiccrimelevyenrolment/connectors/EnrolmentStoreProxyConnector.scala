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

import play.api.http.Status.{ACCEPTED, CREATED, NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.models.KeyValue
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait EnrolmentStoreProxyConnector {
  def getEnrolmentsForGroup(groupId: String)(implicit hc: HeaderCarrier): Future[Option[GroupEnrolmentsResponse]]
  def queryKnownFacts(knownFacts: Seq[KeyValue])(implicit hc: HeaderCarrier): Future[Option[QueryKnownFactsResponse]]
  def queryGroupsWithEnrolment(eclReference: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[QueryGroupsWithEnrolmentResponse]]
}

class EnrolmentStoreProxyConnectorImpl @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2
)(implicit
  ec: ExecutionContext
) extends EnrolmentStoreProxyConnector {

  private val enrolmentStoreUrl: String =
    s"${appConfig.enrolmentStoreProxyBaseUrl}/enrolment-store-proxy/enrolment-store"

  private def deserialiseAs[A](response: HttpResponse)(implicit reads: Reads[A]): Option[A] =
    response.json
      .validate[A]
      .map(result => Some(result))
      .recoverTotal(error => throw new Exception(error.toString))

  def getEnrolmentsForGroup(groupId: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[GroupEnrolmentsResponse]] =
    httpClient
      .get(url"$enrolmentStoreUrl/groups/$groupId/enrolments")
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK | ACCEPTED | CREATED => deserialiseAs[GroupEnrolmentsResponse](response)
          case NO_CONTENT | NOT_FOUND  => None
          case _                       => throw UpstreamErrorResponse(response.body, response.status)
        }
      }

  def queryKnownFacts(knownFacts: Seq[KeyValue])(implicit
    hc: HeaderCarrier
  ): Future[Option[QueryKnownFactsResponse]] =
    httpClient
      .post(url"$enrolmentStoreUrl/enrolments")
      .withBody(
        Json.toJson(
          QueryKnownFactsRequest(
            EclEnrolment.serviceName,
            knownFacts = knownFacts
          )
        )
      )
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK | ACCEPTED | CREATED => deserialiseAs[QueryKnownFactsResponse](response)
          case NO_CONTENT | NOT_FOUND  => None
          case _                       => throw UpstreamErrorResponse(response.body, response.status)
        }
      }

  def queryGroupsWithEnrolment(eclReference: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[QueryGroupsWithEnrolmentResponse]] =
    httpClient
      .get(url"$enrolmentStoreUrl/enrolments/${EclEnrolment.enrolmentKey(eclReference)}/groups?ignore-assignments=true")
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK | ACCEPTED | CREATED => deserialiseAs[QueryGroupsWithEnrolmentResponse](response)
          case NO_CONTENT | NOT_FOUND  => None
          case _                       => throw UpstreamErrorResponse(response.body, response.status)
        }
      }
}

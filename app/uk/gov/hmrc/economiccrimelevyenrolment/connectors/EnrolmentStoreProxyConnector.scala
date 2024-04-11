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

import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.models.KeyValue
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._
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

  def getEnrolmentsForGroup(groupId: String)(implicit hc: HeaderCarrier): Future[Option[GroupEnrolmentsResponse]] =
    httpClient
      .get(url"$enrolmentStoreUrl/groups/$groupId/enrolments")
      .execute[Option[GroupEnrolmentsResponse]]

  def queryKnownFacts(knownFacts: Seq[KeyValue])(implicit hc: HeaderCarrier): Future[Option[QueryKnownFactsResponse]] =
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
      .execute[Option[QueryKnownFactsResponse]]

  def queryGroupsWithEnrolment(eclReference: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[QueryGroupsWithEnrolmentResponse]] =
    httpClient
      .get(url"$enrolmentStoreUrl/enrolments/${EclEnrolment.enrolmentKey(eclReference)}/groups?ignore-assignments=true")
      .execute[Option[QueryGroupsWithEnrolmentResponse]]
}

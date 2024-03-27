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

import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.models.KeyValue
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait EnrolmentStoreProxyConnector {
  def getEnrolmentsForGroup(groupId: String)(implicit hc: HeaderCarrier): Future[Option[GroupEnrolmentsResponse]]
  def queryKnownFacts(knownFacts: Seq[KeyValue])(implicit hc: HeaderCarrier): Future[Option[QueryKnownFactsResponse]]
  def queryGroupsWithEnrolment(eclReference: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[QueryGroupsWithEnrolmentResponse]]
}

class EnrolmentStoreProxyConnectorImpl @Inject() (appConfig: AppConfig, httpClient: HttpClient)(implicit
  ec: ExecutionContext
) extends EnrolmentStoreProxyConnector {

  private val enrolmentStoreUrl: String =
    s"${appConfig.enrolmentStoreProxyBaseUrl}/enrolment-store-proxy/enrolment-store"

  def getEnrolmentsForGroup(groupId: String)(implicit hc: HeaderCarrier): Future[Option[GroupEnrolmentsResponse]] =
    httpClient.GET[Option[GroupEnrolmentsResponse]](
      s"$enrolmentStoreUrl/groups/$groupId/enrolments"
    )(readOptionOfNotFoundOrNoContent[GroupEnrolmentsResponse], hc, ec)

  def queryKnownFacts(knownFacts: Seq[KeyValue])(implicit hc: HeaderCarrier): Future[Option[QueryKnownFactsResponse]] =
    httpClient.POST[QueryKnownFactsRequest, Option[QueryKnownFactsResponse]](
      s"$enrolmentStoreUrl/enrolments",
      QueryKnownFactsRequest(
        EclEnrolment.serviceName,
        knownFacts = knownFacts
      )
    )(QueryKnownFactsRequest.format, readOptionOfNotFoundOrNoContent[QueryKnownFactsResponse], hc, ec)

  def queryGroupsWithEnrolment(eclReference: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[QueryGroupsWithEnrolmentResponse]] =
    httpClient.GET[Option[QueryGroupsWithEnrolmentResponse]](
      s"$enrolmentStoreUrl/enrolments/${EclEnrolment.enrolmentKey(eclReference)}/groups?ignore-assignments=true"
    )(readOptionOfNotFoundOrNoContent[QueryGroupsWithEnrolmentResponse], hc, ec)
}

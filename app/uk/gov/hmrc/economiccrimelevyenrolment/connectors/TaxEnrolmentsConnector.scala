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

import play.api.http.HeaderNames
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{AllocateEnrolmentRequest, EclEnrolment}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxEnrolmentsConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2
)(implicit ec: ExecutionContext) {

  private val taxEnrolmentsUrl: String =
    s"${appConfig.taxEnrolmentsBaseUrl}/tax-enrolments"

  def allocateEnrolment(
    groupId: String,
    eclRegistrationReference: String,
    allocateEnrolmentRequest: AllocateEnrolmentRequest
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val enrolmentKey = s"${EclEnrolment.serviceName}~${EclEnrolment.identifierKey}~$eclRegistrationReference"

    val request = httpClient
      .post(url"$taxEnrolmentsUrl/groups/$groupId/enrolments/$enrolmentKey")
      .withBody(Json.toJson(allocateEnrolmentRequest))

    hc.authorization
      .map(auth => request.setHeader(HeaderNames.AUTHORIZATION -> auth.value))
      .getOrElse(request)
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map {
        case Left(e)  => throw e
        case Right(_) => ()
      }
  }
}

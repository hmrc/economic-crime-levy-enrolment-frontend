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
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{AllocateEnrolmentRequest, EclEnrolment}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxEnrolmentsConnector @Inject() (appConfig: AppConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) {

  private val taxEnrolmentsUrl: String =
    s"${appConfig.taxEnrolmentsBaseUrl}/tax-enrolments"

  def allocateEnrolment(
    groupId: String,
    eclRegistrationReference: String,
    allocateEnrolmentRequest: AllocateEnrolmentRequest
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val enrolmentKey = s"${EclEnrolment.serviceName}~${EclEnrolment.identifierKey}~$eclRegistrationReference"

    httpClient
      .POST[AllocateEnrolmentRequest, Either[UpstreamErrorResponse, HttpResponse]](
        s"$taxEnrolmentsUrl/groups/$groupId/enrolments/$enrolmentKey",
        allocateEnrolmentRequest
      )
      .map {
        case Left(e)  => throw e
        case Right(_) => ()
      }
  }
}

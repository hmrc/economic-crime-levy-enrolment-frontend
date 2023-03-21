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

package uk.gov.hmrc.economiccrimelevyenrolment.testonly.connectors.stubs

import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.connectors.EnrolmentStoreProxyConnector
import uk.gov.hmrc.economiccrimelevyenrolment.models.KeyValue
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{EclEnrolment, Enrolment, GroupEnrolmentsResponse, QueryKnownFactsResponse}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class StubEnrolmentStoreProxyConnector @Inject() (appConfig: AppConfig) extends EnrolmentStoreProxyConnector {
  override def getEnrolmentsForGroup(groupId: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[GroupEnrolmentsResponse]] =
    if (appConfig.enrolmentStoreProxyStubReturnsEclReference) {
      val groupEnrolmentsWithEcl = GroupEnrolmentsResponse(
        Seq(
          Enrolment(
            service = EclEnrolment.ServiceName,
            identifiers = Seq(KeyValue(key = EclEnrolment.IdentifierKey, value = "XMECL0000000001")),
            verifiers = Seq(KeyValue(EclEnrolment.VerifierKey, "20230901"))
          )
        )
      )

      Future.successful(Some(groupEnrolmentsWithEcl))
    } else {
      Future.successful(None)
    }

  def queryKnownFacts(knownFacts: Seq[KeyValue])(implicit hc: HeaderCarrier): Future[QueryKnownFactsResponse] =
    Future.successful(
      QueryKnownFactsResponse(
        service = EclEnrolment.ServiceName,
        enrolments = Seq(
          Enrolment(
            service = EclEnrolment.ServiceName,
            identifiers = Seq(KeyValue(EclEnrolment.IdentifierKey, "XMECL0000000001")),
            verifiers = Seq(KeyValue(EclEnrolment.VerifierKey, "20230901"))
          )
        )
      )
    )

}

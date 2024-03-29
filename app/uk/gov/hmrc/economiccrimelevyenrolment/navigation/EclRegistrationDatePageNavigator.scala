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

import play.api.mvc.Call
import uk.gov.hmrc.economiccrimelevyenrolment.connectors.{EnrolmentStoreProxyConnector, TaxEnrolmentsConnector}
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.models.audit.{ClaimEnrolmentDetailsMismatchAuditEvent, ClaimEnrolmentDetailsMismatchReason, EnrolmentClaimedAuditEvent}
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{AllocateEnrolmentRequest, EclEnrolment}
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.DataRequest
import uk.gov.hmrc.economiccrimelevyenrolment.models.{KeyValue, UserAnswers}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EclRegistrationDatePageNavigator @Inject() (
  enrolmentStoreProxyConnector: EnrolmentStoreProxyConnector,
  taxEnrolmentsConnector: TaxEnrolmentsConnector,
  auditConnector: AuditConnector
)(implicit
  ec: ExecutionContext
) extends AsyncPageNavigator
    with FrontendHeaderCarrierProvider {

  override protected def navigate(userAnswers: UserAnswers)(implicit request: DataRequest[_]): Future[Call] =
    (userAnswers.eclReferenceNumber, userAnswers.eclRegistrationDate) match {
      case (Some(eclReferenceNumber), Some(eclRegistrationDate)) =>
        verifyEclRegistrationDate(eclReferenceNumber, eclRegistrationDate)
      case _                                                     => Future.successful(routes.NotableErrorController.answersAreInvalid())
    }

  private def verifyEclRegistrationDate(eclReferenceNumber: String, eclRegistrationDate: LocalDate)(implicit
    request: DataRequest[_]
  ): Future[Call] = {
    val eclRegistrationDateString = eclRegistrationDate.format(DateTimeFormatter.BASIC_ISO_DATE)

    val knownFacts = Seq(
      KeyValue(key = EclEnrolment.identifierKey, value = eclReferenceNumber),
      KeyValue(key = EclEnrolment.verifierKey, value = eclRegistrationDateString)
    )

    enrolmentStoreProxyConnector.queryKnownFacts(knownFacts).flatMap {
      case Some(response) =>
        response.enrolments.find(e =>
          e.verifiers.exists(_.value == eclRegistrationDateString) && e.identifiers
            .exists(_.value == eclReferenceNumber)
        ) match {
          case Some(_) =>
            allocateEnrolment(eclReferenceNumber, eclRegistrationDateString)
              .map {
                auditConnector.sendExtendedEvent(
                  EnrolmentClaimedAuditEvent(
                    request.internalId,
                    eclReferenceNumber,
                    eclRegistrationDate
                  ).extendedDataEvent
                )
                _ => routes.ConfirmationController.onPageLoad()
              }
          case _       =>
            auditEclRegistrationDateMismatch(eclReferenceNumber, eclRegistrationDate)
            Future.successful(routes.NotableErrorController.detailsDoNotMatch())
        }
      case _              =>
        auditEclRegistrationDateMismatch(eclReferenceNumber, eclRegistrationDate)
        Future.successful(routes.NotableErrorController.detailsDoNotMatch())
    }
  }

  private def allocateEnrolment(eclReferenceNumber: String, eclRegistrationDate: String)(implicit
    request: DataRequest[_]
  ): Future[Unit] = taxEnrolmentsConnector.allocateEnrolment(
    request.groupId,
    eclReferenceNumber,
    AllocateEnrolmentRequest(
      userId = request.credentials.providerId,
      verifiers = Seq(KeyValue(EclEnrolment.verifierKey, eclRegistrationDate))
    )
  )

  private def auditEclRegistrationDateMismatch(eclReferenceNumber: String, eclRegistrationDate: LocalDate)(implicit
    request: DataRequest[_]
  ): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      ClaimEnrolmentDetailsMismatchAuditEvent(
        internalId = request.internalId,
        mismatchReason = ClaimEnrolmentDetailsMismatchReason.EclRegistrationDateMismatch,
        eclReference = eclReferenceNumber,
        eclRegistrationDate = Some(eclRegistrationDate)
      ).extendedDataEvent
    )

}

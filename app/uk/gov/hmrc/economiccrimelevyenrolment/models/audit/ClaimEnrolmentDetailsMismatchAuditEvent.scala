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

package uk.gov.hmrc.economiccrimelevyenrolment.models.audit

import play.api.libs.json._

import java.time.LocalDate

sealed trait ClaimEnrolmentDetailsMismatchReason

object ClaimEnrolmentDetailsMismatchReason {
  case object EclReferenceMismatch extends ClaimEnrolmentDetailsMismatchReason

  case object EclRegistrationDateMismatch extends ClaimEnrolmentDetailsMismatchReason

  implicit val writes: Writes[ClaimEnrolmentDetailsMismatchReason] = o => JsString(o.toString)
}

case class ClaimEnrolmentDetailsMismatchAuditEvent(
  internalId: String,
  mismatchReason: ClaimEnrolmentDetailsMismatchReason,
  eclReference: String,
  eclRegistrationDate: Option[LocalDate]
) extends AuditEvent {
  override val auditType: String   = "ClaimEnrolmentDetailsMismatch"
  override val detailJson: JsValue = Json.toJson(this)
}

object ClaimEnrolmentDetailsMismatchAuditEvent {
  implicit val writes: OWrites[ClaimEnrolmentDetailsMismatchAuditEvent] =
    Json.writes[ClaimEnrolmentDetailsMismatchAuditEvent]
}

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

import play.api.libs.json.JsValue
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

trait AuditEvent {

  private val AuditSource: String = "economic-crime-levy-enrolment-frontend"
  val auditType: String
  val detailJson: JsValue

  def extendedDataEvent: ExtendedDataEvent =
    ExtendedDataEvent(auditSource = AuditSource, auditType = auditType, detail = detailJson)
}

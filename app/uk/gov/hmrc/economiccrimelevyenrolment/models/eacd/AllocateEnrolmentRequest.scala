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

package uk.gov.hmrc.economiccrimelevyenrolment.models.eacd

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.economiccrimelevyenrolment.models.KeyValue

final case class AllocateEnrolmentRequest(
  userId: String,
  friendlyName: String = "My Economic Crime Levy Account",
  qtype: String = "principal", // TODO: The JSON element is 'type' but this is a reserved word
  verifiers: Seq[KeyValue]
)

object AllocateEnrolmentRequest {
  implicit val format: OFormat[AllocateEnrolmentRequest] = Json.format[AllocateEnrolmentRequest]
}

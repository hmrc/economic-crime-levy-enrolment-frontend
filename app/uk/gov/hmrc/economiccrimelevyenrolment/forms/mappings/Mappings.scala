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

package uk.gov.hmrc.economiccrimelevyenrolment.forms.mappings

import play.api.data.FieldMapping
import play.api.data.Forms.of
import play.api.data.validation.Constraint
import uk.gov.hmrc.economiccrimelevyenrolment.models.Enumerable

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {

  protected def sanitise(value: String): String =
    value.filterNot(_.isWhitespace)

  protected def text(
    errorKey: String = "error.required",
    args: Seq[String] = Seq.empty
  ): FieldMapping[String] =
    of(stringFormatter(errorKey, args, sanitise))

  protected def enumerable[A](
    requiredKey: String = "error.required",
    invalidKey: String = "error.invalid",
    args: Seq[String] = Seq.empty
  )(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args, sanitise))

  protected def localDate(
    invalidKey: String,
    requiredKey: String,
    sanitise: Option[String] => Option[String],
    minDateConstraint: Option[Constraint[LocalDate]] = None,
    maxDateConstraint: Option[Constraint[LocalDate]] = None,
    args: Seq[String] = Seq.empty
  ): FieldMapping[LocalDate] =
    of(
      new LocalDateFormatter(
        invalidKey,
        requiredKey,
        sanitise,
        minDateConstraint,
        maxDateConstraint,
        args
      )
    )
}

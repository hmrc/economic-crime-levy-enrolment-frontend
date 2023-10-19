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

package uk.gov.hmrc.economiccrimelevyenrolment.forms

import play.api.data.Form
import uk.gov.hmrc.economiccrimelevyenrolment.forms.mappings.{Mappings, MinMaxValues}

import java.time.LocalDate
import javax.inject.Inject

class EclRegistrationDateFormProvider @Inject() extends Mappings {

  def sanitise(value: String) =
    value.replaceAll(" ", "")

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        "error.date.invalid",
        "error.date.required",
        sanitise,
        Some(
          minDate(
            MinMaxValues.MinEclRegistrationDate,
            "eclRegistrationDate.error.notWithinRange"
          )
        ),
        Some(
          maxDate(
            MinMaxValues.maxEclRegistrationDate,
            "eclRegistrationDate.error.notWithinRange"
          )
        )
      )
    )

}

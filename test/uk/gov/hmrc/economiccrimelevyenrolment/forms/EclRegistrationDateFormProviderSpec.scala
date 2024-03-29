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

import play.api.data.FormError
import uk.gov.hmrc.economiccrimelevyenrolment.forms.behaviours.DateBehaviours
import uk.gov.hmrc.economiccrimelevyenrolment.forms.mappings.MinMaxValues

class EclRegistrationDateFormProviderSpec extends DateBehaviours {
  val form = new EclRegistrationDateFormProvider()()

  "value" should {
    val fieldName   = "value"
    val requiredKey = "error.date.required"

    behave like dateField(
      form,
      fieldName,
      datesBetween(MinMaxValues.minEclRegistrationDate, MinMaxValues.maxEclRegistrationDate)
    )

    behave like mandatoryDateField(
      form,
      fieldName,
      requiredKey
    )

    behave like dateFieldWithMin(
      form,
      fieldName,
      MinMaxValues.minEclRegistrationDate,
      Seq(
        FormError(s"$fieldName.day", "eclRegistrationDate.error.notWithinRange"),
        FormError(s"$fieldName.month", "eclRegistrationDate.error.notWithinRange"),
        FormError(s"$fieldName.year", "eclRegistrationDate.error.notWithinRange")
      )
    )

    behave like dateFieldWithMax(
      form,
      fieldName,
      MinMaxValues.maxEclRegistrationDate,
      Seq(
        FormError(s"$fieldName.day", "eclRegistrationDate.error.notWithinRange"),
        FormError(s"$fieldName.month", "eclRegistrationDate.error.notWithinRange"),
        FormError(s"$fieldName.year", "eclRegistrationDate.error.notWithinRange")
      )
    )
  }
}

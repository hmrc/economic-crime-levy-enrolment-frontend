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
import uk.gov.hmrc.economiccrimelevyenrolment.forms.behaviours.OptionFieldBehaviours

class EclReferenceFormProviderSpec extends OptionFieldBehaviours {
  val form = new EclReferenceFormProvider()()

  "value" should {
    val fieldName   = "value"
    val requiredKey = "eclReference.error.required"

    behave like fieldThatBindsValidData(form, fieldName, nonBlankString)

    behave like mandatoryField(form, fieldName, FormError(fieldName, requiredKey))
  }
}

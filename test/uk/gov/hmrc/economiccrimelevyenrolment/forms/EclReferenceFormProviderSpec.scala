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

import play.api.data.{Form, FormError}
import uk.gov.hmrc.economiccrimelevyenrolment.forms.behaviours.StringFieldBehaviours
import uk.gov.hmrc.economiccrimelevyenrolment.forms.mappings.Regex

class EclReferenceFormProviderSpec extends StringFieldBehaviours {
  val form = new EclReferenceFormProvider()()

  "value" should {
    val fieldName   = "value"
    val requiredKey = "eclReference.error.required"

    behave like fieldThatBindsValidData(form, fieldName, eclRegistrationReference)

    behave like mandatoryField(form, fieldName, FormError(fieldName, requiredKey))

    "fail to bind an invalid registration reference" in forAll(
      stringsWithMaxLength(15).retryUntil(!_.matches(Regex.eclRegistrationReferenceRegex))
    ) { invalidRegistrationReference: String =>
      val result: Form[String] = form.bind(Map("value" -> invalidRegistrationReference))

      result.errors.map(_.message) should contain("eclReference.error.invalid")
    }
  }
}

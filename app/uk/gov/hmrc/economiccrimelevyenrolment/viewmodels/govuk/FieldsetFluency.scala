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

package uk.gov.hmrc.economiccrimelevyenrolment.viewmodels.govuk

import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}

object fieldset extends FieldsetFluency

trait FieldsetFluency {

  object FieldsetViewModel {

    def apply(legend: Legend): Fieldset =
      Fieldset(legend = Some(legend))
  }

  object LegendViewModel {

    def apply(content: Content): Legend =
      Legend(content = content)
  }

  implicit class FluentLegend(legend: Legend) {

    def withCssClass(newClass: String): Legend =
      legend.copy(classes = s"${legend.classes} $newClass")

    def asHidden(): Legend =
      legend
        .copy(isPageHeading = false)
        .withCssClass("govuk-visually-hidden")
  }
}

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
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.models.TriState._
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers

class HasEclReferencePageNavigator extends PageNavigator {

  override protected def navigate(userAnswers: UserAnswers): Call =
    userAnswers.hasEclReference match {
      case Some(Yes)     => routes.EclReferenceController.onPageLoad()
      case Some(No)      => routes.RegistrationController.onPageLoad()
      case Some(Unknown) => routes.FindEclReferenceController.onPageLoad()
      case _             => routes.NotableErrorController.answersAreInvalid()
    }

}

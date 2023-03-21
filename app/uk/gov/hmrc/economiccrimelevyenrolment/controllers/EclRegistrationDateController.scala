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

package uk.gov.hmrc.economiccrimelevyenrolment.controllers

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.actions.{AuthorisedActionWithEnrolmentCheck, DataRetrievalAction}
import uk.gov.hmrc.economiccrimelevyenrolment.forms.EclRegistrationDateFormProvider
import uk.gov.hmrc.economiccrimelevyenrolment.forms.FormImplicits.FormOps
import uk.gov.hmrc.economiccrimelevyenrolment.models.NormalMode
import uk.gov.hmrc.economiccrimelevyenrolment.navigation.EclRegistrationDatePageNavigator
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository
import uk.gov.hmrc.economiccrimelevyenrolment.views.html.EclRegistrationDateView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EclRegistrationDateController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authorise: AuthorisedActionWithEnrolmentCheck,
  getUserAnswers: DataRetrievalAction,
  repository: SessionRepository,
  formProvider: EclRegistrationDateFormProvider,
  pageNavigator: EclRegistrationDatePageNavigator,
  view: EclRegistrationDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[LocalDate] = formProvider()

  def onPageLoad: Action[AnyContent] = (authorise andThen getUserAnswers) { implicit request =>
    Ok(view(form.prepare(request.userAnswers.eclRegistrationDate)))
  }

  def onSubmit: Action[AnyContent] = (authorise andThen getUserAnswers).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        eclRegistrationDate => {
          val updatedAnswers = request.userAnswers.copy(eclRegistrationDate = Some(eclRegistrationDate))
          repository
            .upsert(updatedAnswers)
            .flatMap(_ => pageNavigator.nextPage(NormalMode, updatedAnswers).map(Redirect))
        }
      )
  }

}

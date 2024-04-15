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
import uk.gov.hmrc.economiccrimelevyenrolment.forms.FormImplicits.FormOps
import uk.gov.hmrc.economiccrimelevyenrolment.forms.EclReferenceFormProvider
import uk.gov.hmrc.economiccrimelevyenrolment.navigation.EclReferencePageNavigator
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository
import uk.gov.hmrc.economiccrimelevyenrolment.views.html.EclReferenceView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EclReferenceController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authorise: AuthorisedActionWithEnrolmentCheck,
  getUserAnswers: DataRetrievalAction,
  repository: SessionRepository,
  formProvider: EclReferenceFormProvider,
  pageNavigator: EclReferencePageNavigator,
  view: EclReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad: Action[AnyContent] = (authorise andThen getUserAnswers) { implicit request =>
    Ok(view(form.prepare(request.userAnswers.eclReferenceNumber)))
  }

  def onSubmit: Action[AnyContent] = (authorise andThen getUserAnswers).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        eclReferenceNumber => {
          val updatedAnswers = request.userAnswers.copy(eclReferenceNumber = Some(eclReferenceNumber.toUpperCase()))
          repository
            .upsert(updatedAnswers)
            .flatMap(_ => pageNavigator.nextPage(updatedAnswers).map(Redirect))
        }
      )
  }

}

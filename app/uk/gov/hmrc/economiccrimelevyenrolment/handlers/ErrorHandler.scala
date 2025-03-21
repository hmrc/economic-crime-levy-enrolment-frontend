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

package uk.gov.hmrc.economiccrimelevyenrolment.handlers

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Request, RequestHeader}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import uk.gov.hmrc.economiccrimelevyenrolment.views.html.ErrorTemplate

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject() (
  val messagesApi: MessagesApi,
  view: ErrorTemplate,
  econtext: ExecutionContext
) extends FrontendErrorHandler
    with I18nSupport {

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    rh: Request[_]
  ): Html =
    view(pageTitle, heading, message)

  def internalServerErrorTemplate(implicit request: Request[_]): Html = standardErrorTemplate(
    Messages("error.problemWithService.title"),
    Messages("error.problemWithService.heading"),
    Messages("error.problemWithService.message")
  )

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    request: RequestHeader
  ): Future[Html] = Future.successful(view(pageTitle, heading, message))

  override protected implicit val ec: ExecutionContext = econtext
}

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

package uk.gov.hmrc.economiccrimelevyenrolment.controllers.actions

import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.{AuthorisedRequest, DataRequest}

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalOrErrorAction(data: UserAnswers) extends DataRetrievalOrErrorAction {

  override protected def refine[A](request: AuthorisedRequest[A]): Future[Either[Result, DataRequest[A]]] =
    Option(data) match {
      case Some(data) =>
        Future(Right(DataRequest(request.request, request.internalId, request.groupId, request.credentials, data)))
      case None       =>
        Future(Left(Redirect(routes.NotableErrorController.eclAlreadyAdded())))
    }

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

}

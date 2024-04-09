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

import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.{AuthorisedRequest, DataRequest}
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersDataRetrievalOrErrorAction @Inject() (
  val sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends DataRetrievalOrErrorAction
    with FrontendHeaderCarrierProvider {

  override protected def refine[A](request: AuthorisedRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    sessionRepository.get(request.internalId).map {
      case Some(_) => Future.successful(Right(DataRequest(request.request, request.internalId, request.groupId, request.credentials, _)))
      case None => Left(Redirect(routes.NotableErrorController.eclAlreadyAdded()))
    }

    }

}

trait DataRetrievalOrErrorAction extends ActionRefiner[AuthorisedRequest, DataRequest]

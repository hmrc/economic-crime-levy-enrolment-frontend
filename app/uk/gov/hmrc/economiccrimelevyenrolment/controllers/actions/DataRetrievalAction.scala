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

import play.api.mvc.ActionTransformer
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.{AuthorisedRequest, DataRequest}
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersDataRetrievalAction @Inject() (
  val sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends DataRetrievalAction
    with FrontendHeaderCarrierProvider {

  override protected def transform[A](request: AuthorisedRequest[A]): Future[DataRequest[A]] =
    getOrCreateUserAnswers(request.internalId).map {
      DataRequest(request.request, request.internalId, request.groupId, request.credentials, _)
    }

  private def getOrCreateUserAnswers(internalId: String): Future[UserAnswers] =
    sessionRepository.get(internalId).flatMap {
      case Some(userAnswers) => Future.successful(userAnswers)
      case None              =>
        val userAnswers = UserAnswers.empty(internalId)
        sessionRepository.upsert(userAnswers).map(_ => userAnswers)
    }
}

trait DataRetrievalAction extends ActionTransformer[AuthorisedRequest, DataRequest]

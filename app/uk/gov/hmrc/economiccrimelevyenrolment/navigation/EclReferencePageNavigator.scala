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
import uk.gov.hmrc.economiccrimelevyenrolment.connectors.EnrolmentStoreProxyConnector
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.EclEnrolment
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.DataRequest
import uk.gov.hmrc.economiccrimelevyenrolment.models.{KeyValue, UserAnswers}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EclReferencePageNavigator @Inject() (enrolmentStoreProxyConnector: EnrolmentStoreProxyConnector)(implicit
  ec: ExecutionContext
) extends AsyncPageNavigator
    with FrontendHeaderCarrierProvider {

  override protected def navigate(userAnswers: UserAnswers)(implicit request: DataRequest[_]): Future[Call] =
    userAnswers.eclReferenceNumber match {
      case Some(eclReferenceNumber) => verifyEclReferenceNumber(eclReferenceNumber)
      case _                        => Future.successful(routes.NotableErrorController.answersAreInvalid())
    }

  private def verifyEclReferenceNumber(eclReferenceNumber: String)(implicit request: DataRequest[_]): Future[Call] = {
    val knownFacts = Seq(
      KeyValue(key = EclEnrolment.IdentifierKey, value = eclReferenceNumber)
    )

    enrolmentStoreProxyConnector.queryKnownFacts(knownFacts).map {
      case Some(response) =>
        response.enrolments.find(_.identifiers.exists(_.value == eclReferenceNumber)) match {
          case Some(_) => routes.EclRegistrationDateController.onPageLoad()
          case _       => routes.NotableErrorController.detailsDoNotMatch()
        }
      case _              => routes.NotableErrorController.detailsDoNotMatch()
    }
  }

}

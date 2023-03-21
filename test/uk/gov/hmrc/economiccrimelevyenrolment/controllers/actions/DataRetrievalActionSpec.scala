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

import org.mockito.ArgumentMatchers
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.models.requests.{AuthorisedRequest, DataRequest}
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository

import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  class TestDataRetrievalAction extends UserAnswersDataRetrievalAction(mockSessionRepository) {
    override def transform[A](request: AuthorisedRequest[A]): Future[DataRequest[A]] =
      super.transform(request)
  }

  val dataRetrievalAction =
    new TestDataRetrievalAction

  val testAction: Request[_] => Future[Result] = { _ =>
    Future(Ok("Test"))
  }

  "transform" should {
    "transform an AuthorisedRequest into a DataRequest" in forAll {
      (internalId: String, groupId: String, userAnswers: UserAnswers) =>
        when(mockSessionRepository.get(ArgumentMatchers.eq(internalId))).thenReturn(Future(Some(userAnswers)))

        val result: Future[DataRequest[AnyContentAsEmpty.type]] =
          dataRetrievalAction.transform(AuthorisedRequest(fakeRequest, internalId, groupId, None))

        await(result) shouldBe DataRequest(fakeRequest, internalId, userAnswers)
    }
  }

}

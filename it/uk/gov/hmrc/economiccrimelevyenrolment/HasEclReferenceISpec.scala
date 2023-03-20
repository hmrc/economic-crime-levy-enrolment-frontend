/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevyenrolment

import com.danielasfregola.randomdatagenerator.RandomDataGenerator.random
import org.mockito.ArgumentMatchers
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyenrolment.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.behaviours.AuthorisedBehaviour
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository

import scala.concurrent.Future

class HasEclReferenceISpec extends ISpecBase with AuthorisedBehaviour {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  s"GET ${routes.HasEclReferenceController.onPageLoad().url}" should {
    behave like authorisedActionWithEnrolmentCheckRoute(routes.HasEclReferenceController.onPageLoad())

    "respond with 200 status and the do you have an ECL reference number HTML view" in {
      stubAuthorisedWithEclEnrolment()

      val userAnswers = random[UserAnswers]

      when(mockSessionRepository.get(ArgumentMatchers.eq(testInternalId)))
        .thenReturn(Future.successful(Some(userAnswers)))

      val result = callRoute(FakeRequest(routes.HasEclReferenceController.onPageLoad()))

      status(result) shouldBe OK

      html(result) should include("Do you have an Economic Crime Levy (ECL) reference number?")
    }
  }

}

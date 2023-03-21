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

import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.TriState.No
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.views.html.RegistrationView

import scala.concurrent.Future

class RegistrationControllerSpec extends SpecBase {

  val view: RegistrationView = app.injector.instanceOf[RegistrationView]

  class TestContext(userAnswers: UserAnswers) {
    val controller = new RegistrationController(
      mcc,
      fakeAuthorisedActionWithEnrolmentCheck(userAnswers.internalId),
      view
    )
  }

  "onPageLoad" should {
    "return OK and the correct view" in forAll { userAnswers: UserAnswers =>
      new TestContext(userAnswers.copy(hasEclReference = Some(No))) {
        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) shouldBe OK

        contentAsString(result) shouldBe view()(fakeRequest, messages).toString
      }
    }
  }

}

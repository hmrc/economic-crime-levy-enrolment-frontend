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

package uk.gov.hmrc.economiccrimelevyenrolment.base

import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers.{stubBodyParser, stubControllerComponents}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import uk.gov.hmrc.economiccrimelevyenrolment.EclTestData
import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.actions._
import uk.gov.hmrc.economiccrimelevyenrolment.generators.Generators
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpVerbs.GET

import scala.concurrent.ExecutionContext

trait SpecBase
    extends AnyWordSpec
    with Matchers
    with TryValues
    with OptionValues
    with DefaultAwaitTimeout
    with FutureAwaits
    with Results
    with GuiceOneAppPerSuite
    with MockitoSugar
    with BeforeAndAfterEach
    with ScalaCheckPropertyChecks
    with EclTestData
    with Generators {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val ec: ExecutionContext     = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier        = HeaderCarrier()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val appConfig: AppConfig                             = app.injector.instanceOf[AppConfig]
  val messages: Messages                               = messagesApi.preferred(fakeRequest)
  val bodyParsers: PlayBodyParsers                     = app.injector.instanceOf[PlayBodyParsers]

  def fakeAuthorisedActionWithEnrolmentCheck(internalId: String, groupId: String, providerId: String) =
    new FakeAuthorisedActionWithEnrolmentCheck(internalId, groupId, providerId, bodyParsers)
  def fakeAuthorisedActionWithoutEnrolmentCheck(
    internalId: String,
    groupId: String,
    providerId: String,
    eclRegistrationReference: Option[String] = None
  )                                                                                                   =
    new FakeAuthorisedActionWithoutEnrolmentCheck(
      eclRegistrationReference,
      internalId,
      groupId,
      providerId,
      bodyParsers
    )
  def fakeAuthorisedActionAgentsAllowed                                                               =
    new FakeAuthorisedActionAgentsAllowed(bodyParsers)
  def fakeAuthorisedActionAssistantsAllowed                                                           =
    new FakeAuthorisedActionAssistantsAllowed(bodyParsers)
  def fakeDataRetrievalAction(data: UserAnswers)                                                      =
    new FakeDataRetrievalAction(data)
  def fakeDataRetrievalOrErrorAction(data: UserAnswers)                                               =
    new FakeDataRetrievalOrErrorAction(data)

  def onwardRoute: Call = Call(GET, "/foo")

  val mcc: DefaultMessagesControllerComponents = {
    val stub = stubControllerComponents()
    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), stub.messagesApi)(stub.executionContext),
      DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
      stub.parsers,
      messagesApi,
      stub.langs,
      stub.fileMimeTypes,
      stub.executionContext
    )
  }

}

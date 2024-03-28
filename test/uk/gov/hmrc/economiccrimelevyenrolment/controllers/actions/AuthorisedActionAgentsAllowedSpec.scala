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
import org.mockito.ArgumentMatchers.any
import play.api.mvc.{BodyParsers, Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.economiccrimelevyenrolment.EnrolmentsWithEcl
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.controllers.routes
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.EclEnrolment
import uk.gov.hmrc.economiccrimelevyenrolment.services.EnrolmentStoreProxyService

import scala.concurrent.Future

class AuthorisedActionAgentsAllowedSpec extends SpecBase {

  val defaultBodyParser: BodyParsers.Default                     = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector                           = mock[AuthConnector]
  val mockEnrolmentStoreProxyService: EnrolmentStoreProxyService = mock[EnrolmentStoreProxyService]

  val authorisedAction =
    new AuthorisedActionAgentsAllowedImpl(
      mockAuthConnector,
      mockEnrolmentStoreProxyService,
      appConfig,
      defaultBodyParser
    )

  val testAction: Request[_] => Future[Result] = { _ =>
    Future(Ok("Test"))
  }

  val eclEnrolmentKey: String = EclEnrolment.serviceName

  val expectedRetrievals: Retrieval[
    Option[String] ~ Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]
  ] =
    Retrievals.internalId and Retrievals.allEnrolments and Retrievals.groupIdentifier and Retrievals.affinityGroup and Retrievals.credentialRole and Retrievals.credentials

  "invokeBlock" should {
    "execute the block and return the result if authorised" in forAll {
      (
        internalId: String,
        enrolmentsWithEcl: EnrolmentsWithEcl,
        groupId: String,
        affinityGroup: AffinityGroup,
        credentials: Credentials
      ) =>
        when(mockAuthConnector.authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any()))
          .thenReturn(
            Future(
              Some(internalId) and enrolmentsWithEcl.enrolments and Some(groupId) and Some(affinityGroup) and Some(
                User
              ) and Some(credentials)
            )
          )

        when(mockEnrolmentStoreProxyService.getEclReferenceFromGroupEnrolment(ArgumentMatchers.eq(groupId))(any()))
          .thenReturn(Future.successful(None))

        val result: Future[Result] = authorisedAction.invokeBlock(fakeRequest, testAction)

        status(result)          shouldBe OK
        contentAsString(result) shouldBe "Test"
    }

    "redirect the user to sign in when there is no active session" in {
      List(BearerTokenExpired(), MissingBearerToken(), InvalidBearerToken(), SessionRecordNotFound()).foreach {
        exception =>
          when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.failed(exception))

          val result: Future[Result] = authorisedAction.invokeBlock(fakeRequest, testAction)

          status(result)               shouldBe SEE_OTHER
          redirectLocation(result).value should startWith(appConfig.signInUrl)
      }
    }

    "redirect the user to the assistant not supported page if they have an assistant credential role" in forAll {
      (internalId: String, enrolmentsWithEcl: EnrolmentsWithEcl, groupId: String, credentials: Credentials) =>
        when(
          mockAuthConnector
            .authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any())
        )
          .thenReturn(
            Future(
              Some(internalId) and enrolmentsWithEcl.enrolments and Some(groupId) and Some(Organisation) and Some(
                Assistant
              ) and Some(credentials)
            )
          )

        val result: Future[Result] = authorisedAction.invokeBlock(fakeRequest, testAction)

        status(result)                 shouldBe SEE_OTHER
        redirectLocation(result).value shouldBe routes.NotableErrorController.assistantCannotRegister().url
    }

    "throw an IllegalStateException if there is no internal id" in forAll { credentials: Credentials =>
      when(mockAuthConnector.authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any()))
        .thenReturn(
          Future(
            None and Enrolments(Set.empty) and Some("") and Some(Organisation) and Some(User) and Some(credentials)
          )
        )

      val result = intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }

      result.getMessage shouldBe "Unable to retrieve internalId"
    }

    "throw an IllegalStateException if there is no group id" in forAll { credentials: Credentials =>
      when(mockAuthConnector.authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any()))
        .thenReturn(
          Future(
            Some("") and Enrolments(Set.empty) and None and Some(Organisation) and Some(User) and Some(credentials)
          )
        )

      val result = intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }

      result.getMessage shouldBe "Unable to retrieve groupIdentifier"
    }

    "throw an IllegalStateException if there is no affinity group" in forAll { credentials: Credentials =>
      when(mockAuthConnector.authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any()))
        .thenReturn(
          Future(Some("") and Enrolments(Set.empty) and Some("") and None and Some(User) and Some(credentials))
        )

      val result = intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }

      result.getMessage shouldBe "Unable to retrieve affinityGroup"
    }

    "throw an IllegalStateException if there is no credential role" in forAll { credentials: Credentials =>
      when(mockAuthConnector.authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any()))
        .thenReturn(
          Future(Some("") and Enrolments(Set.empty) and Some("") and Some(Organisation) and None and Some(credentials))
        )

      val result = intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }

      result.getMessage shouldBe "Unable to retrieve credentialRole"
    }

    "throw an IllegalStateException if there are no credentials" in {
      when(mockAuthConnector.authorise(any(), ArgumentMatchers.eq(expectedRetrievals))(any(), any()))
        .thenReturn(
          Future(Some("") and Enrolments(Set.empty) and Some("") and Some(Organisation) and Some(User) and None)
        )

      val result = intercept[IllegalStateException] {
        await(authorisedAction.invokeBlock(fakeRequest, testAction))
      }

      result.getMessage shouldBe "Unable to retrieve credentials"
    }
  }

}

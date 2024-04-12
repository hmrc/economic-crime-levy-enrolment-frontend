package uk.gov.hmrc.economiccrimelevyenrolment.base

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyenrolment.base.WireMockHelper.stub
import uk.gov.hmrc.economiccrimelevyenrolment.models.TriState.Yes
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository

trait SessionRepositoryStubs { self: WireMockStubs =>

  def stubUpsert(sessionRepository: SessionRepository, userAnswers: UserAnswers) =
    sessionRepository.upsert(userAnswers)
}

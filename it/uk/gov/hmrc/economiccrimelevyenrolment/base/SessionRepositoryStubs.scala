package uk.gov.hmrc.economiccrimelevyenrolment.base

import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository

import scala.concurrent.Future

trait SessionRepositoryStubs { self: WireMockStubs =>

  def stubUpsert(sessionRepository: SessionRepository, userAnswers: UserAnswers): Future[Boolean] =
    sessionRepository.upsert(userAnswers)
}

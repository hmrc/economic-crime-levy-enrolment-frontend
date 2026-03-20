package uk.gov.hmrc.economiccrimelevyenrolment.repositories

import org.scalatestplus.mockito.MockitoSugar
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import org.mockito.Mockito.when
import org.mockito.Mockito.doReturn
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with MockitoSugar {

  private val now              = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(now, ZoneId.systemDefault)

  private val mockAppConfig = mock[AppConfig]
  doReturn(1).when(mockAppConfig).mongoTtl

  private val userAnswers =
    UserAnswers
      .empty(internalId = "test-id")
      .copy(lastUpdated = Some(Instant.ofEpochSecond(1)))

  private lazy val sessionRepository: SessionRepository =
    new SessionRepository(
      mongoComponent = mongoComponent,
      appConfig = mockAppConfig,
      clock = stubClock
    )

  override protected val repository = sessionRepository

  "upsert" should {
    "insert a new user answers document with the last updated time set to `now`" in {
      val expectedResult = userAnswers.copy(lastUpdated = Some(now))

      val setResult     = sessionRepository.upsert(userAnswers).futureValue
      val updatedRecord = super.find(Filters.equal("internalId", userAnswers.internalId)).futureValue.headOption.value

      setResult     shouldEqual true
      updatedRecord shouldEqual expectedResult
    }

    "update an existing user answers document with the last updated time set to `now`" in {
      super.insert(userAnswers).futureValue

      val expectedResult = userAnswers.copy(lastUpdated = Some(now))

      val setResult     = sessionRepository.upsert(userAnswers).futureValue
      val updatedRecord = super.find(Filters.equal("internalId", userAnswers.internalId)).futureValue.headOption.value

      setResult     shouldEqual true
      updatedRecord shouldEqual expectedResult
    }
  }

  "get" should {
    "update the lastUpdated time and get the record when there is a record for the id" in {
      super.insert(userAnswers.copy(lastUpdated = Some(now))).futureValue

      val result         = sessionRepository.get(userAnswers.internalId).futureValue
      val expectedResult = userAnswers.copy(lastUpdated = Some(now))

      result.value shouldEqual expectedResult
    }

    "return None when there is no record for the id" in {
      sessionRepository.get("id that does not exist").futureValue should not be defined
    }
  }

  "clear" should {
    "remove a record" in {
      super.insert(userAnswers).futureValue

      val result = sessionRepository.clear(userAnswers.internalId).futureValue

      result                                               shouldEqual true
      sessionRepository.get(userAnswers.internalId).futureValue should not be defined
    }

    "return true when there is no record to remove" in {
      val result = sessionRepository.clear("id that does not exist").futureValue

      result shouldEqual true
    }
  }

  "keepAlive" should {
    "update lastUpdated to `now` and return true when there is a record for the id" in {
      super.insert(userAnswers).futureValue

      val result = sessionRepository.keepAlive(userAnswers.internalId).futureValue

      val expectedRegistration = userAnswers.copy(lastUpdated = Some(now))

      result shouldEqual true
      val updatedRegistration =
        super.find(Filters.equal("internalId", userAnswers.internalId)).futureValue.headOption.value
      updatedRegistration shouldEqual expectedRegistration
    }

    "return true when there is no record for the id" in {
      sessionRepository.keepAlive("id that does not exist").futureValue shouldEqual true
    }
  }
}

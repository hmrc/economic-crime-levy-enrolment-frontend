package uk.gov.hmrc.economiccrimelevyenrolment.repositories

import org.mockito.MockitoSugar
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.models.UserAnswers
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec
    extends AnyWordSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val now              = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(now, ZoneId.systemDefault)

  private val userAnswers = UserAnswers
    .empty(
      internalId = "test-id"
    )
    .copy(lastUpdated = Some(Instant.ofEpochSecond(1)))

  private val mockAppConfig = mock[AppConfig]

  when(mockAppConfig.mongoTtl) thenReturn 1

  protected override val repository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  "upsert" should {
    "insert a new user answers document with the last updated time set to `now`" in {
      val expectedResult = userAnswers.copy(lastUpdated = Some(now))

      val setResult     = repository.upsert(userAnswers).futureValue
      val updatedRecord = find(Filters.equal("internalId", userAnswers.internalId)).futureValue.headOption.value

      setResult     shouldEqual true
      updatedRecord shouldEqual expectedResult
    }

    "update an existing user answers document with the last updated time set to `now`" in {
      insert(userAnswers).futureValue

      val expectedResult = userAnswers.copy(lastUpdated = Some(now))

      val setResult     = repository.upsert(userAnswers).futureValue
      val updatedRecord = find(Filters.equal("internalId", userAnswers.internalId)).futureValue.headOption.value

      setResult     shouldEqual true
      updatedRecord shouldEqual expectedResult
    }
  }

  "get" should {
    "update the lastUpdated time and get the record when there is a record for the id" in {
      insert(userAnswers.copy(lastUpdated = Some(now))).futureValue

      val result         = repository.get(userAnswers.internalId).futureValue
      val expectedResult = userAnswers.copy(lastUpdated = Some(now))

      result.value shouldEqual expectedResult
    }

    "return None when there is no record for the id" in {
      repository.get("id that does not exist").futureValue should not be defined
    }
  }

  "clear" should {
    "remove a record" in {
      insert(userAnswers).futureValue

      val result = repository.clear(userAnswers.internalId).futureValue

      result                                         shouldEqual true
      repository.get(userAnswers.internalId).futureValue should not be defined
    }

    "return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result shouldEqual true
    }
  }

  "keepAlive" should {
    "update lastUpdated to `now` and return true when there is a record for the id" in {
      insert(userAnswers).futureValue

      val result = repository.keepAlive(userAnswers.internalId).futureValue

      val expectedRegistration = userAnswers.copy(lastUpdated = Some(now))

      result shouldEqual true
      val updatedRegistration = find(Filters.equal("internalId", userAnswers.internalId)).futureValue.headOption.value
      updatedRegistration shouldEqual expectedRegistration
    }

    "return true when there is no record for the id" in {
      repository.keepAlive("id that does not exist").futureValue shouldEqual true
    }
  }
}

/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.economiccrimelevyenrolment.base

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.jsoup.Jsoup
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Status => _, _}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http._
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Result, Results}
import play.api.test._
import play.api.{Application, Mode}
import uk.gov.hmrc.economiccrimelevyenrolment.base.WireMockHelper.setWireMockPort
import uk.gov.hmrc.economiccrimelevyenrolment.config.AppConfig
import uk.gov.hmrc.economiccrimelevyenrolment.generators.Generators
import uk.gov.hmrc.economiccrimelevyenrolment.repositories.SessionRepository

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

abstract class ISpecBase
    extends AnyWordSpec
    with GuiceOneAppPerSuite
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with Matchers
    with DefaultAwaitTimeout
    with Writeables
    with RouteInvokers
    with Results
    with Status
    with HeaderNames
    with MimeTypes
    with ResultExtractors
    with OptionValues
    with WireMockHelper
    with WireMockStubs
    with Generators {

  implicit val arbString: Arbitrary[String]    = Arbitrary(Gen.alphaNumStr.retryUntil(_.nonEmpty))
  implicit lazy val system: ActorSystem        = ActorSystem()
  implicit lazy val materializer: Materializer = Materializer(system)
  implicit def ec: ExecutionContext            = global

  val additionalAppConfig: Map[String, Any] = Map(
    "metrics.enabled"                         -> false,
    "auditing.enabled"                        -> false,
    "features.enrolmentStoreProxyStubEnabled" -> false
  ) ++ setWireMockPort(
    "auth",
    "enrolment-store-proxy",
    "tax-enrolments"
  )

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .configure(additionalAppConfig)
      .in(Mode.Test)
      .build()

  val appConfig: AppConfig                 = app.injector.instanceOf[AppConfig]
  implicit val messagesApi: MessagesApi    = app.injector.instanceOf[MessagesApi]
  val sessionRepository: SessionRepository = app.injector.instanceOf[SessionRepository]

  /*
  This is to initialise the app before running any tests, as it is lazy by default in org.scalatestplus.play.BaseOneAppPerSuite.
  It enables us to include behaviour tests that call routes within the `should` part of a test but before `in`.
   */
  locally { val _ = app }

  override def beforeAll(): Unit = {
    startWireMock()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  override protected def afterEach(): Unit = {
    resetWireMock()
    super.afterEach()
  }

  def callRoute[A](fakeRequest: FakeRequest[A], requiresAuth: Boolean = true)(implicit
    app: Application,
    w: Writeable[A]
  ): Future[Result] = {
    val errorHandler = app.errorHandler

    val req = if (requiresAuth) fakeRequest.withSession("authToken" -> "test") else fakeRequest

    route(app, req) match {
      case None         => fail("Route does not exist")
      case Some(result) =>
        result.recoverWith { case t: Throwable =>
          errorHandler.onServerError(req, t)
        }
    }
  }

  def html(result: Future[Result]): String = {
    contentType(result) shouldBe Some("text/html")
    Jsoup.parse(contentAsString(result)).html()
  }

}

package uk.gov.hmrc.economiccrimelevyenrolment.base

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyenrolment.base.WireMockHelper.stub
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.AllocateEnrolmentRequest

trait TaxEnrolmentsStubs { self: WireMockStubs =>

  def stubAllocateEnrolment(allocateEnrolmentRequest: AllocateEnrolmentRequest): StubMapping = {
    val groupId = s"$testGroupId"
    val enrolmentKey = s"HMRC-ECL-ORG~EclRegistrationReference~$testEclRegistrationReference"

    stub(
      post(urlEqualTo(s"/tax-enrolments/groups/$groupId/enrolments/$enrolmentKey"))
        .withRequestBody(equalToJson(Json.toJson(allocateEnrolmentRequest).toString())),
      aResponse()
        .withStatus(201)
    )
  }
}

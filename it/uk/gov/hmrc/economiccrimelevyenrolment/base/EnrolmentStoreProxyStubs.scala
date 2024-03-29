package uk.gov.hmrc.economiccrimelevyenrolment.base

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyenrolment.base.WireMockHelper.stub
import uk.gov.hmrc.economiccrimelevyenrolment.models.KeyValue
import uk.gov.hmrc.economiccrimelevyenrolment.models.eacd.{EclEnrolment, Enrolment, QueryKnownFactsRequest, QueryKnownFactsResponse}

trait EnrolmentStoreProxyStubs { self: WireMockStubs =>

  def stubQueryKnownFacts(eclRegistrationReference: String): StubMapping =
    stub(
      post(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/enrolments")).withRequestBody(
        equalToJson(
          Json
            .toJson(
              QueryKnownFactsRequest(
                service = EclEnrolment.serviceName,
                knownFacts = Seq(
                  KeyValue(key = EclEnrolment.identifierKey, value = eclRegistrationReference)
                )
              )
            )
            .toString()
        )
      ),
      aResponse()
        .withStatus(OK)
        .withBody(
          Json
            .toJson(
              QueryKnownFactsResponse(
                service = EclEnrolment.serviceName,
                enrolments = Seq(
                  Enrolment(
                    identifiers = Seq(KeyValue(key = EclEnrolment.identifierKey, value = eclRegistrationReference)),
                    verifiers = Seq.empty
                  )
                )
              )
            )
            .toString()
        )
    )

  def stubQueryKnownFacts(eclRegistrationReference: String, eclRegistrationDate: String): StubMapping =
    stub(
      post(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/enrolments")).withRequestBody(
        equalToJson(
          Json
            .toJson(
              QueryKnownFactsRequest(
                service = EclEnrolment.serviceName,
                knownFacts = Seq(
                  KeyValue(key = EclEnrolment.identifierKey, value = eclRegistrationReference),
                  KeyValue(key = EclEnrolment.verifierKey, value = eclRegistrationDate)
                )
              )
            )
            .toString()
        )
      ),
      aResponse()
        .withStatus(OK)
        .withBody(
          Json
            .toJson(
              QueryKnownFactsResponse(
                service = EclEnrolment.serviceName,
                enrolments = Seq(
                  Enrolment(
                    identifiers = Seq(KeyValue(key = EclEnrolment.identifierKey, value = eclRegistrationReference)),
                    verifiers = Seq(KeyValue(key = EclEnrolment.verifierKey, value = eclRegistrationDate))
                  )
                )
              )
            )
            .toString()
        )
    )

  def stubNoGroupEnrolment(): StubMapping =
    stub(
      get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$testGroupId/enrolments")),
      aResponse()
        .withStatus(NO_CONTENT)
    )

  def stubGroupsWithEnrolment(eclReference: String): StubMapping =
    stub(
      get(
        urlEqualTo(
          s"/enrolment-store-proxy/enrolment-store/enrolments/${EclEnrolment.enrolmentKey(eclReference)}/groups?ignore-assignments=true"
        )
      ),
      aResponse()
        .withStatus(NO_CONTENT)
    )

  def stubWithGroupEclEnrolment(): StubMapping =
    stub(
      get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$testGroupId/enrolments")),
      aResponse()
        .withStatus(OK)
        .withBody(s"""
            |{
            |    "startRecord": 1,
            |    "totalRecords": 1,
            |    "enrolments": [
            |        {
            |           "service": "${EclEnrolment.serviceName}",
            |           "state": "Activated",
            |           "friendlyName": "ECL Enrolment",
            |           "enrolmentDate": "2023-10-05T14:48:00.000Z",
            |           "failedActivationCount": 0,
            |           "activationDate": "2023-10-13T17:36:00.000Z",
            |           "identifiers": [
            |              {
            |                 "key": "${EclEnrolment.identifierKey}",
            |                 "value": "$testEclRegistrationReference"
            |              }
            |           ],
            |           "verifiers": [
            |              {
            |                 "key": "${EclEnrolment.verifierKey}",
            |                 "value": "$testEclRegistrationDateString"
            |              }
            |           ]
            |        }
            |    ]
            |}
            |""".stripMargin)
    )

}

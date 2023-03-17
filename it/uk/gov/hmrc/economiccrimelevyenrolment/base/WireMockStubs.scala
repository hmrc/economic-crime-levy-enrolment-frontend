/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.economiccrimelevyenrolment.base

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.economiccrimelevyenrolment.EclTestData

trait WireMockStubs
    extends EclTestData
    with AuthStubs
    with EnrolmentStoreProxyStubs {

  def stubAuthorisedWithNoGroupEnrolment(): StubMapping = {
    stubAuthorised()
    stubNoGroupEnrolment()
  }
}

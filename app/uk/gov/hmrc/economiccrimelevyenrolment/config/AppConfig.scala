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

package uk.gov.hmrc.economiccrimelevyenrolment.config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = configuration.get[String]("contact-frontend.serviceId")
  private val exitSurveyHost               = configuration.get[String]("feedback-frontend.host")
  private val exitSurveyServiceIdentifier  = configuration.get[String]("feedback-frontend.serviceId")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${RedirectUrl(host + request.uri)}"

  def feedbackUrl(backUrl: String): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${RedirectUrl(backUrl)}"

  val appName: String                                     = configuration.get[String]("appName")
  val countdown: Int                                      = configuration.get[Int]("timeout-dialog.countdown")
  val eclAccountEnabled: Boolean                          = configuration.get[Boolean]("features.eclAccountEnabled")
  val eclAccountUrl: String                               = configuration.get[String]("urls.eclAccount")
  val enrolmentStoreProxyBaseUrl: String                  = servicesConfig.baseUrl("enrolment-store-proxy")
  val enrolmentStoreProxyStubReturnsEclReference: Boolean =
    configuration.get[Boolean]("features.enrolmentStoreProxyStubReturnsEclReference")
  val exitSurveyUrl: String                               = s"$exitSurveyHost/feedback/$exitSurveyServiceIdentifier"
  val host: String                                        = configuration.get[String]("host")
  val languageTranslationEnabled: Boolean                 = configuration.get[Boolean]("features.welsh-translation")
  val mongoTtl: Int                                       = configuration.get[Int]("mongodb.timeToLiveInSeconds")
  val registrationUrl: String                             = configuration.get[String]("urls.registration")
  val signInUrl: String                                   = configuration.get[String]("urls.signIn")
  val signOutUrl: String                                  = configuration.get[String]("urls.signOut")
  val submitReturnUrl: String                             = configuration.get[String]("urls.submitReturn")
  val taxAndSchemeManagementUrl: String                   = configuration.get[String]("urls.taxAndSchemeManagement")
  val taxEnrolmentsBaseUrl: String                        = servicesConfig.baseUrl("tax-enrolments")
  val timeout: Int                                        = configuration.get[Int]("timeout-dialog.timeout")
  val businessAccountUrl: String                          = configuration.get[String]("urls.businessAccount")
}

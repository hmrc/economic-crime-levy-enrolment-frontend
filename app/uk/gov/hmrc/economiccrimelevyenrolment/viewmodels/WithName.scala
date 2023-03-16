package uk.gov.hmrc.economiccrimelevyenrolment.viewmodels

abstract class WithName(name: String) {
  override val toString: String = name
}

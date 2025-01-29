import sbt._

object AppDependencies {

  private val hmrcBootstrapVersion = "9.7.0"
  private val hmrcMongoVersion     = "2.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % hmrcBootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "11.11.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.2.0"
  )

  val test: Seq[ModuleID]    = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"   % hmrcBootstrapVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion,
    "org.jsoup"             % "jsoup"                    % "1.18.3",
    "org.mockito"          %% "mockito-scala"            % "1.17.37",
    "org.scalatestplus"    %% "scalacheck-1-17"          % "3.2.18.0",
    "com.danielasfregola"  %% "random-data-generator"    % "2.9",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"    % "1.1.0"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test

}

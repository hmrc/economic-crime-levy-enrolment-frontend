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

package uk.gov.hmrc.economiccrimelevyenrolment.models

import play.api.libs.json.{JsBoolean, JsError, JsString, Json}
import uk.gov.hmrc.economiccrimelevyenrolment.base.SpecBase
import uk.gov.hmrc.economiccrimelevyenrolment.generators.CachedArbitraries._

class TriStateSpec extends SpecBase {
  "writes" should {
    "return the tri-state serialized to its JSON representation" in forAll { triSate: TriState =>
      val result = Json.toJson(triSate)

      result shouldBe JsString(triSate.toString)
    }
  }

  "reads" should {
    "return the tri-state deserialized from its JSON representation" in forAll { triSate: TriState =>
      val json = Json.toJson(triSate)

      json.as[TriState] shouldBe triSate
    }

    "return a JsError when passed an invalid string value" in {
      val result = Json.fromJson[TriState](JsString("Test"))

      result shouldBe JsError("Test is not a valid TriState")
    }

    "return a JsError when passed a type that is not a string" in {
      val result = Json.fromJson[TriState](JsBoolean(true))

      result shouldBe a[JsError]
    }
  }
}

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

import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait TriState

object TriState {
  case object Yes extends TriState
  case object No extends TriState
  case object Unknown extends TriState

  val values: Seq[TriState] = Seq(
    Yes,
    No,
    Unknown
  )

  def options(contextKey: String, divider: Boolean = true)(implicit messages: Messages): Seq[RadioItem] =
    values.zipWithIndex.flatMap {
      case (value, index) if value == Unknown && divider =>
        Seq(
          RadioItem(
            divider = Some(messages(s"$contextKey.divider"))
          ),
          RadioItem(
            content = Text(messages(s"$contextKey.${value.toString}")),
            value = Some(value.toString),
            id = Some(s"value_$index")
          )
        )
      case (value, index)                                =>
        Seq(
          RadioItem(
            content = Text(messages(s"$contextKey.${value.toString}")),
            value = Some(value.toString),
            id = Some(s"value_$index")
          )
        )
    }

  implicit val enumerable: Enumerable[TriState] = Enumerable(values.map(v => (v.toString, v)): _*)

  implicit val format: Format[TriState] = new Format[TriState] {
    override def reads(json: JsValue): JsResult[TriState] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "Yes"     => JsSuccess(Yes)
          case "No"      => JsSuccess(No)
          case "Unknown" => JsSuccess(Unknown)
          case s         => JsError(s"$s is not a valid TriState")
        }
      case e: JsError          => e
    }

    override def writes(o: TriState): JsValue = JsString(o.toString)
  }
}

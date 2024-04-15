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

package uk.gov.hmrc.economiccrimelevyenrolment.forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter
import uk.gov.hmrc.economiccrimelevyenrolment.models.Enumerable

trait Formatters {

  private[mappings] def stringFormatter(
    errorKey: String,
    args: Seq[String] = Seq.empty,
    sanitise: String => String
  ): Formatter[String] =
    new Formatter[String] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data.get(key) match {
          case None    => Left(Seq(FormError(key, errorKey, args)))
          case Some(s) =>
            sanitise(s) match {
              case ss if ss.isEmpty => Left(Seq(FormError(key, errorKey, args)))
              case ss               => Right(ss)
            }
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    }

  private[mappings] def enumerableFormatter[A](
    requiredKey: String,
    invalidKey: String,
    args: Seq[String] = Seq.empty,
    sanitise: String => String
  )(implicit
    ev: Enumerable[A]
  ): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey, args, sanitise)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).flatMap { str =>
          ev.value(str)
            .map(Right.apply)
            .getOrElse(Left(Seq(FormError(key, invalidKey, args))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }
}

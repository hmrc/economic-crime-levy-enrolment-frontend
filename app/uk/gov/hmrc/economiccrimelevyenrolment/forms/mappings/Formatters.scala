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

import scala.util.control.Exception.nonFatalCatch

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

  private[mappings] def booleanFormatter(
    requiredKey: String,
    invalidKey: String,
    args: Seq[String] = Seq.empty,
    sanitise: String => String
  ): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args, sanitise)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] =
        baseFormatter
          .bind(key, data)
          .flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey, args)))
          }

      def unbind(key: String, value: Boolean): Map[String, String] = Map(key -> value.toString)
    }

  private def numberFormatter[T](
    stringToNumber: String => T,
    requiredKey: String,
    wholeNumberKey: String,
    nonNumericKey: String,
    args: Seq[String],
    sanitise: String => String
  ): Formatter[T] =
    new Formatter[T] {

      val decimalRegexp = """^-?(\d*\.\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey, args, sanitise)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .map(_.replaceAll("\\s+", ""))
          .flatMap {
            case s if s.matches(decimalRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s                             =>
              nonFatalCatch
                .either(stringToNumber(s))
                .left
                .map(_ => Seq(FormError(key, nonNumericKey, args)))
          }

      override def unbind(key: String, value: T): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def longFormatter(
    requiredKey: String,
    wholeNumberKey: String,
    nonNumericKey: String,
    args: Seq[String] = Seq.empty,
    sanitise: String => String
  ): Formatter[Long] = numberFormatter[Long](_.toLong, requiredKey, wholeNumberKey, nonNumericKey, args, sanitise)

  private[mappings] def intFormatter(
    requiredKey: String,
    wholeNumberKey: String,
    nonNumericKey: String,
    args: Seq[String] = Seq.empty,
    sanitise: String => String
  ): Formatter[Int] = numberFormatter[Int](_.toInt, requiredKey, wholeNumberKey, nonNumericKey, args, sanitise)

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

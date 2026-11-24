/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.formpproxy.shared.utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.BindingKey
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}

class DatabaseExecutionContextProviderSpec extends AnyFreeSpec with Matchers {

  "the @Named(\"database\") ExecutionContext" - {
    "must run work on the dedicated database dispatcher, not the default one" in {
      val app = new GuiceApplicationBuilder()
        .configure("play.http.router" -> "prod.Routes")
        .build()
      try {
        val dbEc      = app.injector.instanceOf(BindingKey(classOf[ExecutionContext]).qualifiedWith("database"))
        val defaultEc = app.injector.instanceOf[ExecutionContext]

        val dbThread      = Await.result(Future(Thread.currentThread.getName)(dbEc), 5.seconds)
        val defaultThread = Await.result(Future(Thread.currentThread.getName)(defaultEc), 5.seconds)

        dbThread      must include("database.dispatcher")
        defaultThread must not include "database.dispatcher"
      } finally app.stop()
    }
  }
}

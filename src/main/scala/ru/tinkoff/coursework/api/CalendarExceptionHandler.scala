package ru.tinkoff.coursework.api

import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.ExceptionHandler
import ru.tinkoff.coursework.CalendarException


object CalendarExceptionHandler {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: CalendarException => complete(BadRequest, ExceptionResponse(s"This is error ~ ${e.getMessage}"))
    }
}

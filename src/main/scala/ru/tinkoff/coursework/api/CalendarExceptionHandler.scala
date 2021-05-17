package ru.tinkoff.coursework.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.ExceptionHandler
import ru.tinkoff.coursework.{CalendarException, CalendarWarning, EventNotFoundException}


object CalendarExceptionHandler {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: EventNotFoundException => complete(StatusCodes.NotFound, ExceptionResponse(e.getMessage))
      case e: CalendarException => complete(BadRequest, ExceptionResponse(s"Error: ${e.getMessage}"))
      case _: IllegalArgumentException => complete(BadRequest, ExceptionResponse("Wrong argument format."))
      case e: CalendarWarning => complete(StatusCodes.OK, ExceptionResponse(s"Warning: ${e.getMessage}"))
    }
}

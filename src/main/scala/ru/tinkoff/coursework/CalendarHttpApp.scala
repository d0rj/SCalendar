package ru.tinkoff.coursework

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import ru.tinkoff.coursework.api.{CalendarExceptionHandler, UserCalendarApi}
import ru.tinkoff.coursework.controllers.{CalendarService, CalendarServiceImpl}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


object CalendarHttpApp {
  implicit val ac: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = ac.dispatcher

  def main(args: Array[String]): Unit = {
    Await.result(CalendarServiceMain().start(), Duration.Inf)
    ()
  }
}


case class CalendarServiceMain()(implicit ac: ActorSystem, ec: ExecutionContext) extends LazyLogging {

  private val calendarService: CalendarService = new CalendarServiceImpl()
  private val userCalendarApi: UserCalendarApi = new UserCalendarApi(calendarService)
  private val routes = Route.seal(
      userCalendarApi.route
  )(exceptionHandler = CalendarExceptionHandler.exceptionHandler)

  def start(): Future[Http.ServerBinding] =
    Http()
      .newServerAt("localhost", 8080)
      .bind(routes)
      .andThen { case b => logger.info(s"server started at: $b") }
}
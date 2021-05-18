package ru.tinkoff.coursework

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import ru.tinkoff.coursework.api.{CalendarExceptionHandler, UserCalendarApi}
import ru.tinkoff.coursework.controllers.{CalendarService, CalendarServiceImpl, GoogleCalendarService, ThirdPartyService}

import scala.concurrent.{ExecutionContext, Future}


object CalendarHttpApp extends App {
  implicit val ac: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = ac.dispatcher


  CalendarServiceMain().start()
}


case class CalendarServiceMain()(implicit ac: ActorSystem, ec: ExecutionContext) extends LazyLogging {

  private val calendarService: CalendarService = new CalendarServiceImpl()
  private val googleCalendarService: CalendarService with ThirdPartyService = new GoogleCalendarService
  private val userCalendarApi: UserCalendarApi = new UserCalendarApi(calendarService, googleCalendarService)
  private val routes = Route.seal(userCalendarApi.route)(exceptionHandler = CalendarExceptionHandler.exceptionHandler)


  def start(): Future[Http.ServerBinding] =
    Http()
      .newServerAt("localhost", 8080)
      .bind(routes)
      .andThen { case b => logger.info(s"server started at: $b") }
}
package ru.tinkoff.coursework.api

import akka.http.scaladsl.model.StatusCodes
import ru.tinkoff.coursework.controllers.{CalendarService, ThirdPartyService}
import akka.http.scaladsl.server.Route
import ru.tinkoff.coursework.storage.Event
import akka.http.scaladsl.unmarshalling.Unmarshaller
import ru.tinkoff.coursework.EventNotFoundException
import ru.tinkoff.coursework.logic.AsyncBcryptImpl
import slick.jdbc.MySQLProfile.api.Database

import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}


class UserCalendarApi(calendarService: CalendarService, googleCalendarService: CalendarService with ThirdPartyService)
                     (implicit db: Database, ec: ExecutionContext){
  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._


  val stringToTimestamp: Unmarshaller[String, Timestamp] = Unmarshaller.strict[String, Timestamp](Timestamp.valueOf)

  private val findBetween = (path("events" / "between")
    & parameter("from".?) & parameter("to".?)) { (from, to) =>
    (from, to) match {
      case (None, Some(right)) => get { complete(calendarService.earlier(Timestamp.valueOf(right))) }
      case (Some(left), None) => get { complete(calendarService.later(Timestamp.valueOf(left))) }
      case (Some(left), Some(right)) =>
        val fromTime = Timestamp.valueOf(left)
        val toTime = Timestamp.valueOf(right)
        complete(calendarService.allBetween(fromTime, toTime))
      case _ => get { complete(StatusCodes.BadRequest) }
    }
  }

  private val addNew = (path("events" / "add")
    & parameter("title")
    & parameter("summary")
    & parameter("kind" ? "Simple event")
    & parameter("repeating".as[Boolean].withDefault(false))
    & parameter("date".as(stringToTimestamp))
    & parameter("duration".as[Long].withDefault(0))
    & parameter("location".?)) {
    (title, summary, kind, repeating, date, duration, location) => post {
      val newEvent = new Event(
        id = new AsyncBcryptImpl().hash(title + summary + kind),
        title = title,
        summary = summary,
        kind = kind,
        repeating = repeating,
        date = date,
        location = location,
        duration = duration.toLong
      )

      complete(calendarService.newEvent(newEvent))
    }
  }

  private val deleteEvent = path("events" / """.*""".r) {
    eventId => delete {
      complete(calendarService.removeEvent(eventId))
    }
  }


  private val updateEvent = (path("events" / """.*""".r)
    & parameter("title".?)
    & parameter("summary".?)
    & parameter("repeating".as[Boolean].?)
    & parameter("location".?)) {
    (eventId, title, summary, repeating, location) => put {
      val oldEvent = calendarService.getEvent(eventId)
      val newEvent = oldEvent.flatMap {
        case None => Future.failed(new EventNotFoundException)
        case Some(event) => Future.successful(event.copy(
            title = title getOrElse event.title,
            summary = summary getOrElse event.summary,
            repeating = repeating getOrElse event.repeating,
            location = location
        ))
      }

      val updates: Seq[Future[Unit]] = Seq(
        newEvent.flatMap { e =>
          if (e.kind == "calendar#event")
            googleCalendarService.updateEvent(eventId, e).map { _ => () }
          else
            Future.successful(())
        },
        newEvent.map { calendarService.updateEvent(eventId, _) }.map { _ => () }
      )

      complete(Future.sequence(updates).map { identity })
    }
  }


  private val moveEvent = (path("events" / """.*""".r / "move") & parameter("newDate".as(stringToTimestamp))) {
    (eventId, newDate) => post {
      complete(calendarService.moveEvent(eventId, newDate))
    }
  }

  private val syncWithGoogle = (path("events" / "sync" / "google")
    & parameter("from".as(stringToTimestamp).?)
    & parameter("to".as(stringToTimestamp).?)) {
      (from, to) => post {
        complete(googleCalendarService.synchronize(from, to))
      }
  }


  def route: Route =
    findBetween ~ addNew ~ deleteEvent ~ moveEvent ~ syncWithGoogle ~ updateEvent
}

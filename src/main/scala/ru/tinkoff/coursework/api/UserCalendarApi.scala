package ru.tinkoff.coursework.api

import akka.http.scaladsl.model.StatusCodes
import ru.tinkoff.coursework.controllers.CalendarService
import akka.http.scaladsl.server.Route
import ru.tinkoff.coursework.storage.Event
import akka.http.scaladsl.unmarshalling.Unmarshaller
import ru.tinkoff.coursework.logic.AsyncBcryptImpl

import java.sql.Timestamp


class UserCalendarApi(calendarService: CalendarService) {
  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._


  val stringToTimestamp: Unmarshaller[String, Timestamp] = Unmarshaller.strict[String, Timestamp](Timestamp.valueOf)

  private val findBetween = (path("events" / "between")
    & parameter("from".?) & parameter("to".?)) { (from, to) =>
    (from, to) match {
      case (None, Some(right)) => get { complete(calendarService.earlier(Timestamp.valueOf(right))) }
      case (Some(left), None) => get { complete(calendarService.later(Timestamp.valueOf(left))) }
      case (Some(left), Some(right)) => {
        val fromTime = Timestamp.valueOf(left)
        val toTime = Timestamp.valueOf(right)
        complete(calendarService.allBetween(fromTime, toTime))
      }
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
        completed = false,
        duration = duration.toLong
      )

      complete(calendarService.newEvent(newEvent))
    }
  }

  private val endEvent = path("events" / """.*""".r / "end") {
    eventId => post {
      complete(calendarService.completeEvent(eventId))
    }
  }

  private val deleteEvent = path("events" / """.*""".r / "delete") {
    eventId => post {
      complete(calendarService.removeEvent(eventId))
    }
  }

  private val moveEvent = (path("events" / """.*""".r / "move") & parameter("newDate".as(stringToTimestamp))) {
    (eventId, newDate) => post {
      complete(calendarService.moveEvent(eventId, newDate))
    }
  }

//  private val syncWithGoogle = path("events" / "sync" / "google" & parameter("calendarId")) {
//    get {
//
//    }
//  }

  def route: Route =
    findBetween ~ addNew ~ endEvent ~ deleteEvent ~ moveEvent
}

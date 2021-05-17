package ru.tinkoff.coursework.controllers
import ru.tinkoff.coursework.EventNotFoundException
import ru.tinkoff.coursework.storage.{Event, EventsQueryRepository}

import java.sql.Timestamp
import scala.concurrent.{Await, Future}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class CalendarServiceImpl extends CalendarService {
  private val db = Database.forConfig("mysqlDB")

  Await.result(db.run(EventsQueryRepository.AllEvents.schema.createIfNotExists), Duration.Inf)


  override def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.between(from, to))


  override def later(from: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.later(from))


  override def earlier(to: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.earlier(to))


  override def newEvent(event: Event): Future[Boolean] =
    db.run(EventsQueryRepository.addEvent(event))
      .map {
        case 0 => false
        case _ => true
      }


  override def removeEvent(eventId: String): Future[Boolean] = {
    db.run(EventsQueryRepository.getEvent(eventId)).flatMap {
      case Some(event) =>
        db.run(EventsQueryRepository.removeEvent(event))
          .map {
            case 0 => false
            case _ => true
          }
      case None => Future.successful(false)
    }
  }


  override def moveEvent(eventId: String, to: Timestamp): Future[Boolean] = {
    // sync logic

    db.run(EventsQueryRepository.changeDatetime(eventId, to)).map {
      case 0 => false
      case _ => true
    }
  }

  override def synchronize(calendarId: String, from: Option[Timestamp], to: Option[Timestamp]): Future[Boolean] =
    throw new EventNotFoundException
}

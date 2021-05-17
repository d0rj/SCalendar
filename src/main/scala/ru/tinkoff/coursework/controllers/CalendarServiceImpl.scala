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


  override def getEvent(eventId: String): Future[Option[Event]] =
    db.run(EventsQueryRepository.getEvent(eventId))


  override def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.between(from, to))


  override def later(from: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.later(from))


  override def earlier(to: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.earlier(to))


  override def newEvent(event: Event): Future[Boolean] =
    db.run(EventsQueryRepository.addEvent(event))
      .map { _ > 0 }


  override def updateEvent(eventId: String, updated: Event): Future[Boolean] = {
    val query: Seq[DBIO[Int]] = Seq(
      EventsQueryRepository.changeTitle(_, updated.title),
      EventsQueryRepository.changeSummary(_, updated.summary),
      EventsQueryRepository.changeLocation(_, updated.location),
      EventsQueryRepository.changeRepeating(_, updated.repeating)
    ).map { _(eventId) }
    db.run(DBIO.sequence(query))
      .map { _.sum }
      .map { _ >= 4 }
  }


  override def removeEvent(eventId: String): Future[Boolean] = {
    db.run(EventsQueryRepository.getEvent(eventId)).flatMap {
      case Some(event) => db.run(EventsQueryRepository.removeEvent(event)).map { _ > 0}
      case None => throw new EventNotFoundException
    }
  }


  override def moveEvent(eventId: String, to: Timestamp): Future[Boolean] = {
    // sync logic

    db.run(EventsQueryRepository.getEvent(eventId)).flatMap {
      case Some(_) => db.run(EventsQueryRepository.changeDatetime(eventId, to)).map { _ > 0 }
      case None => throw new EventNotFoundException
    }
  }
}

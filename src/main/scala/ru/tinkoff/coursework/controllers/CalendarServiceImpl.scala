package ru.tinkoff.coursework.controllers
import ru.tinkoff.coursework.{EventNotFoundException, EventsConflictException}
import ru.tinkoff.coursework.storage.{Event, EventsQueryRepository}
import slick.dbio.DBIO

import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api.Database


class CalendarServiceImpl(db: Database)(implicit ec: ExecutionContext) extends CalendarService {
  override def getEvent(eventId: String): Future[Event] =
    db.run(EventsQueryRepository.getEvent(eventId))
      .flatMap {
        case Some(value) => Future.successful(value)
        case None => Future.failed(new EventNotFoundException)
      }


  override def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.between(from, to))


  override def later(from: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.later(from))


  override def earlier(to: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.earlier(to))


  override def newEvent(event: Event): Future[Unit] =
    db.run(EventsQueryRepository.addEvent(event))
      .map { _ => () }


  override def updateEvent(eventId: String, updated: Event): Future[Unit] = {
    val query: Seq[DBIO[Int]] = Seq(
      EventsQueryRepository.changeTitle(_, updated.title),
      EventsQueryRepository.changeSummary(_, updated.summary),
      EventsQueryRepository.changeLocation(_, updated.location),
      EventsQueryRepository.changeRepeating(_, updated.repeating)
    ).map { _(eventId) }
    db.run(DBIO.sequence(query))
      .map { _ => () }
  }


  override def removeEvent(eventId: String): Future[Unit] =
    db.run(EventsQueryRepository.getEvent(eventId)).flatMap {
      case Some(event) => db.run(EventsQueryRepository.removeEvent(event)).map { _ => () }
      case None => Future.failed(new EventNotFoundException)
    }


  override def moveEvent(eventId: String, to: Timestamp): Future[Unit] = {
    db.run(EventsQueryRepository.getEvent(eventId)).flatMap {
      case Some(event) =>
        val newEnd = new Timestamp(to.getTime + event.duration)
        db.run(EventsQueryRepository.between(to, newEnd).map { _.length }).flatMap {
          case count if count == 0 => db.run(EventsQueryRepository.changeDatetime(eventId, to)).map { _ => () }
          case _ => Future.failed(new EventsConflictException)
        }
      case None => Future.failed(new EventNotFoundException)
    }
  }
}

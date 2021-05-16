package ru.tinkoff.coursework.controllers
import ru.tinkoff.coursework.storage.{Event, EventsQueryRepository}

import java.sql.Timestamp
import scala.concurrent.Future

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global


class CalendarServiceImpl extends CalendarService {
  private val db = Database.forConfig("mysqlDB")


  override def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.between(from, to))

  override def newEvent(event: Event): Future[Boolean] =
    db.run(EventsQueryRepository.addEvent(event))
      .map {
        case 0 => false
        case _ => true
      }

  override def completeEvent(eventId: Int): Future[Boolean] =
    db.run(EventsQueryRepository.changeCompleted(eventId, newCompleted = true))
      .map {
        case 0 => false
        case _ => true
      }

  override def removeEvent(eventId: Int): Future[Boolean] = {
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

  override def moveEvent(eventId: Int, to: Timestamp): Future[Boolean] = {
    // sync logic

    db.run(EventsQueryRepository.changeDatetime(eventId, to)).map {
      case 0 => false
      case _ => true
    }
  }
}

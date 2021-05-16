package ru.tinkoff.coursework.controllers
import ru.tinkoff.coursework.storage.{Event, EventsQueryRepository}

import java.sql.Timestamp
import scala.concurrent.{Await, Future}
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class CalendarServiceImpl extends CalendarService {
  private val db = Database.forConfig("mysqlDB")

  val tables = List(EventsQueryRepository.AllEvents)
  val existing: Future[Vector[MTable]] = db.run(MTable.getTables)
  val f: Future[List[Unit]] = existing.flatMap(v => {
    val names = v.map(mt => mt.name.name)
    val createIfNotExist = tables.filter( table =>
      !names.contains(table.baseTableRow.tableName)).map(_.schema.create)
    db.run(DBIO.sequence(createIfNotExist))
  })
  Await.result(f, Duration.Inf)


  override def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]] =
    db.run(EventsQueryRepository.between(from, to))

  override def newEvent(event: Event): Future[Boolean] =
    db.run(EventsQueryRepository.addEvent(event))
      .map {
        case 0 => false
        case _ => true
      }

  override def completeEvent(eventId: String): Future[Boolean] =
    db.run(EventsQueryRepository.changeCompleted(eventId, newCompleted = true))
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
}

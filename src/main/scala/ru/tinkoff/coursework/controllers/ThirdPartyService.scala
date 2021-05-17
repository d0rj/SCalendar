package ru.tinkoff.coursework.controllers

import ru.tinkoff.coursework.EventNotFoundException
import ru.tinkoff.coursework.storage.EventsQueryRepository

import java.sql.Timestamp
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global


trait ThirdPartyService {
  this: CalendarService =>

  def synchronize(from: Option[Timestamp], to: Option[Timestamp]): Future[Boolean] = {
    val events = (from, to) match {
      case (Some(left), Some(right)) => allBetween(left, right)
      case (Some(left), None) => later(left)
      case (None, Some(right)) => earlier(right)
      case (None, None) => throw new EventNotFoundException
    }

    val db = Database.forConfig("mysqlDB")
    val allEvents = Await.result(db.run(EventsQueryRepository.AllEvents.result), Duration.Inf)

    events
      .map { _.map { e =>
        if (allEvents.exists { _.id == e.id })
          DBIO.sequence(Seq(
            EventsQueryRepository.changeTitle(_, e.title),
            EventsQueryRepository.changeSummary(_, e.summary),
            EventsQueryRepository.changeLocation(_, e.location),
            EventsQueryRepository.changeRepeating(_, e.repeating),
          ).map { _(e.id) })
            .map { _.sum }
        else
          EventsQueryRepository.addEvent(e)}
      }
      .map { _.map { db.run(_) } }
      .flatMap { Future.sequence(_) }
      .map { _.sum }
      .map { _ > 0 }
  }
}

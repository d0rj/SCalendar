package ru.tinkoff.coursework.controllers

import ru.tinkoff.coursework.storage.EventsQueryRepository
import slick.dbio.DBIO

import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api.Database


trait ThirdPartyService {
  this: CalendarService =>

  def synchronize(from: Option[Timestamp], to: Option[Timestamp], db: Database)
                 (implicit ec: ExecutionContext): Future[Unit] = {
    val events = (from, to) match {
      case (Some(left), Some(right)) => allBetween(left, right)
      case (Some(left), None) => later(left)
      case (None, Some(right)) => earlier(right)
      case (None, None) => Future.failed(new IllegalArgumentException)
    }

    db.run(EventsQueryRepository.all)
      .flatMap { allEvents =>
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
              EventsQueryRepository.addEvent(e)
          }}
        .map { _.map { db.run(_) } }
        .map { _ => () }
      }
  }
}

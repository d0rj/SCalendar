package ru.tinkoff.coursework.storage

import ru.tinkoff.coursework.rdbms.DIO
import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

import java.sql.Timestamp


object EventsQueryRepository {
  private val AllEvents = TableQuery[EventsTable]

  private def eventById(id: Int): Query[EventsTable, Event, Seq] =
    AllEvents.filter { _.eventId === id }

  def changeDatetime(id: Int, timestamp: Timestamp): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.date }
      .update { timestamp }

  def changeTitle(id: Int, newTitle: String): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.title }
      .update { newTitle }

  def changeSummary(id: Int, newSummary: String): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.summary }
      .update { newSummary }

  def changeLocation(id: Int, newLocation: Option[String]): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.location }
      .update { newLocation }

  def changeRepeating(id: Int, newRepeating: Boolean): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.repeating }
      .update { newRepeating }

  def changeCompleted(id: Int, newCompleted: Boolean): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.completed }
      .update { newCompleted }

  def getEvent(id: Int): DIO[Option[Event], Effect.Read] =
    eventById(id)
      .result.headOption

  def addEvent(event: Event): DBIOAction[Int, NoStream, Effect.Write] =
    AllEvents += event

  def removeEvent(event: Event): DBIOAction[Int, NoStream, Effect.Write] =
    AllEvents
      .filter { _.eventId === event.id }
      .delete

  def earlier(date: Timestamp): DIO[Seq[Event], Effect.Read] =
    AllEvents
      .filter { _.date <= date }
      .result

  def later(date: Timestamp): DIO[Seq[Event], Effect.Read] =
    AllEvents
      .filter { _.date >= date }
      .result

  def between(from: Timestamp, to: Timestamp): DIO[Seq[Event], Effect.Read] =
    AllEvents
      .filter { _.date <= to }
      .filter { _.date >= from }
      .result
}

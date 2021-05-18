package ru.tinkoff.coursework.storage

import ru.tinkoff.coursework.rdbms.DIO
import slick.dbio.Effect
import slick.jdbc.MySQLProfile.api._

import java.sql.Timestamp
import scala.concurrent.ExecutionContext


object EventsQueryRepository {
  val AllEvents = TableQuery[EventsTable]

  Database.forConfig("mysqlDB").run(AllEvents.schema.createIfNotExists)


  private def eventById(id: String): Query[EventsTable, Event, Seq] =
    AllEvents.filter { _.eventId === id }


  def all: DIO[Seq[Event], Effect.Read] =
    AllEvents.result


  def changeDatetime(id: String, timestamp: Timestamp): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.date }
      .update { timestamp }


  def changeTitle(id: String, newTitle: String): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.title }
      .update { newTitle }


  def changeSummary(id: String, newSummary: String): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.summary }
      .update { newSummary }


  def changeLocation(id: String, newLocation: Option[String]): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.location }
      .update { newLocation }


  def changeRepeating(id: String, newRepeating: Boolean): DIO[Int, Effect.Write] =
    eventById(id)
      .map { _.repeating }
      .update { newRepeating }


  def getEvent(id: String): DIO[Option[Event], Effect.Read] =
    eventById(id)
      .result.headOption


  def addEvent(event: Event): DBIOAction[Int, NoStream, Effect.Write] =
    AllEvents += event


  def addEvents(events: Seq[Event])(implicit ex: ExecutionContext): DBIOAction[Int, NoStream, Effect.Write] =
    (AllEvents ++= events).map { _.sum }


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

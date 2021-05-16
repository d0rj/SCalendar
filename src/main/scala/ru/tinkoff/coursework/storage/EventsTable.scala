package ru.tinkoff.coursework.storage

import java.sql.Timestamp
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape


class EventsTable(tag: Tag) extends Table[Event](tag, "EVENTS") {

  def eventId: Rep[String] = column("ID", O.PrimaryKey)

  def kind: Rep[String] = column("KIND")

  def date: Rep[Timestamp] = column[Timestamp]("DATE", O.SqlType("timestamp default now()"))

  def duration: Rep[Long] = column("DURATION")

  def title: Rep[String] = column("TITLE")

  def summary: Rep[String] = column("SUMMARY")

  def location: Rep[Option[String]] = column("LOCATION")

  def repeating: Rep[Boolean] = column("IS_REPEATING")

  def completed: Rep[Boolean] = column("COMPLETED")


  override def * : ProvenShape[Event] =
    (eventId, kind, date, duration, title, summary, location, repeating, completed) <>
      ((Event.apply _).tupled, Event.unapply)
}

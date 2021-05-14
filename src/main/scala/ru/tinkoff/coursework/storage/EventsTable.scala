package ru.tinkoff.coursework.storage

import java.sql.Timestamp

import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape


class EventsTable(tag: Tag) extends Table[Event](tag, "EVENTS") {

  def eventId: Rep[Int] = column("EVENT_ID", O.PrimaryKey)

  def kind: Rep[String] = column("EVENT_KIND")

  def date: Rep[Timestamp] = column[Timestamp]("DATE", O.SqlType("EVENT_START"))

  def duration: Rep[Long] = column("EVENT_DURATION")

  def title: Rep[String] = column("EVENT_TITLE")

  def summary: Rep[String] = column("EVENT_SUMMARY")

  def location: Rep[Option[String]] = column("EVENT_LOCATION")

  def repeating: Rep[Boolean] = column("EVENT_IS_REPEATING")

  def completed: Rep[Boolean] = column("EVENT_COMPLETED")


  override def * : ProvenShape[Event] =
    (eventId, kind, date, duration, title, summary, location, repeating, completed) <>
      ((Event.apply _).tupled, Event.unapply)
}

package ru.tinkoff.coursework.controllers

import ru.tinkoff.coursework.storage.Event

import java.sql.Timestamp
import scala.concurrent.Future


trait CalendarService {
  def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]]

  def later(from: Timestamp): Future[Seq[Event]]

  def earlier(to: Timestamp): Future[Seq[Event]]

  def newEvent(event: Event): Future[Boolean]

  def removeEvent(eventId: String): Future[Boolean]

  def moveEvent(eventId: String, to: Timestamp): Future[Boolean]

  def synchronize(calendarId: String, from: Option[Timestamp], to: Option[Timestamp]): Future[Boolean]

  // def invite(userId: Int, eventId: Int)

  // def notify(eventId: Int)
}

package ru.tinkoff.coursework.controllers

import ru.tinkoff.coursework.storage.Event

import java.sql.Timestamp
import scala.concurrent.Future


trait CalendarService {
  def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]]

  def newEvent(event: Event): Future[Boolean]

  def completeEvent(eventId: Int): Future[Boolean]

  def removeEvent(eventId: Int): Future[Boolean]

  def moveEvent(eventId: Int, to: Timestamp): Future[Boolean]

  // def synchronize(userId: Int): Future[Boolean]

  // def invite(userId: Int, eventId: Int)

  // def notify(eventId: Int)
}

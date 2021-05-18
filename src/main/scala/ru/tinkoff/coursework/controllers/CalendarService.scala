package ru.tinkoff.coursework.controllers

import ru.tinkoff.coursework.storage.Event

import java.sql.Timestamp
import scala.concurrent.Future


trait CalendarService {
  def getEvent(eventId: String): Future[Option[Event]]

  def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]]

  def later(from: Timestamp): Future[Seq[Event]]

  def earlier(to: Timestamp): Future[Seq[Event]]

  def newEvent(event: Event): Future[Unit]

  def updateEvent(eventId: String, updated: Event): Future[Unit]

  def removeEvent(eventId: String): Future[Unit]

  def moveEvent(eventId: String, to: Timestamp): Future[Unit]
}

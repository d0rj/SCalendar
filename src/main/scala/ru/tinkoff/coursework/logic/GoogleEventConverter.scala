package ru.tinkoff.coursework.logic

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.{Event, EventDateTime}
import ru.tinkoff.coursework.storage

import java.sql.Timestamp


object GoogleEventConverter extends EventConverter[Event] {
  implicit def eventDateTimeToTimestamp(eventDateTime: EventDateTime): Timestamp = eventDateTime.getDate match {
    case null => new Timestamp(eventDateTime.getDateTime.getValue)
    case some => new Timestamp(some.getValue)
  }


  implicit def timestampToEventDateTime(timestamp: Timestamp): DateTime =
    new DateTime(timestamp.getTime)


  override def convert(anotherEvent: Event): storage.Event =
    new storage.Event(
      id = anotherEvent.getId,
      title = anotherEvent.getSummary,
      summary = anotherEvent.getDescription,
      date = anotherEvent.getStart,
      kind = anotherEvent.getKind,
      duration = anotherEvent.getEnd.getDate.getValue - anotherEvent.getStart.getDate.getValue,
      location = Option(anotherEvent.getLocation),
      repeating = anotherEvent.getEndTimeUnspecified,
      completed = if (anotherEvent.getEndTimeUnspecified) false
                else (anotherEvent.getEnd.getDate.getValue - System.currentTimeMillis()) == 0
    )
}

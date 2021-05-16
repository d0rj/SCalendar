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


  implicit def timestampToDateTime(timestamp: Timestamp): DateTime =
    new DateTime(timestamp.getTime)


  implicit def timestampToEventDateTime(timestamp: Timestamp): EventDateTime =
    new EventDateTime()
      .setDateTime(timestamp)
      .setTimeZone("Europe/Moscow")


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


  override def convert(event: storage.Event): Event =
    new Event()
      .setSummary(event.title)
      .setDescription(event.summary)
      .setStart(event.date)
      .setEnd(if (event.duration > 0) new Timestamp(event.date.getTime + event.duration) else event.date)
      .setKind("calendar#event")
      .setLocation(event.location match {
        case Some(l) => l
        case None => ""
      })
      .setEndTimeUnspecified(event.repeating)
}

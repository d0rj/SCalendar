package ru.tinkoff.coursework.logic

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.{Event, EventDateTime}
import ru.tinkoff.coursework.storage

import java.sql.Timestamp


object GoogleEventConverter extends EventConverter[Event] {
  implicit def eventDateTimeToTimestamp(eventDateTime: EventDateTime): Timestamp =
    if (Option(eventDateTime.getDate).isDefined)
      new Timestamp(eventDateTime.getDate.getValue)
    else
      new Timestamp(eventDateTime.getDateTime.getValue)


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
      summary = Option(anotherEvent.getDescription).getOrElse(""),
      date = anotherEvent.getStart,
      kind = anotherEvent.getKind,
      duration =
        if (Option(anotherEvent.getEnd.getDate).isDefined)
          anotherEvent.getEnd.getDate.getValue - anotherEvent.getStart.getDate.getValue
        else
          anotherEvent.getEnd.getDateTime.getValue - anotherEvent.getStart.getDateTime.getValue,
      location =
        if (Option(anotherEvent.getLocation).isDefined)
          Option(anotherEvent.getLocation)
        else None,
      repeating = anotherEvent.getEndTimeUnspecified
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

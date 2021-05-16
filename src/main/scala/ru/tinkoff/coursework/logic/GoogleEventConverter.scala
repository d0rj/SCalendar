package ru.tinkoff.coursework.logic

import com.google.api.services.calendar.model.Event
import ru.tinkoff.coursework.storage


class GoogleEventConverter extends EventConverter[Event] {
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

package ru.tinkoff.coursework


sealed abstract class CalendarException(message: String) extends Exception(message)

final case class EventNotFoundException(message: String = "Event not found") extends CalendarException(message)


sealed abstract class CalendarWarning(message: String) extends Exception(message)

final abstract class AllUpdatedWarning extends CalendarWarning("All already updated.")

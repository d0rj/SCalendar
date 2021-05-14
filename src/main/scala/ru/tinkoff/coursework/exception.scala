package ru.tinkoff.coursework


sealed abstract class CalendarException(message: String) extends Exception(message)

final case class EventNotFoundException() extends CalendarException(s"Events not found")

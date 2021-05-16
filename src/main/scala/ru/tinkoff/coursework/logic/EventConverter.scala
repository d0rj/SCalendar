package ru.tinkoff.coursework.logic

import ru.tinkoff.coursework.storage.Event


trait EventConverter[T] {
  def convert(anotherEvent: T): Event
}

package ru.tinkoff.coursework

import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}


package object rdbms {
  type DIO[+R, -E <: Effect] = DBIOAction[R, NoStream, E]
  val DIO = DBIO
}

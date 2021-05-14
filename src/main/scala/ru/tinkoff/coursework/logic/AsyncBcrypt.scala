package ru.tinkoff.coursework.logic

import com.github.t3hnar.bcrypt.BCryptStrOps
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.blocking


trait AsyncBcrypt {
  def hash(password: String, rounds: Int = 12): String

  def verify(password: String, hash: String): Boolean
}


class AsyncBcryptImpl extends AsyncBcrypt with StrictLogging {
  override def hash(password: String, rounds: Int): String =
    blocking(password.bcryptBounded(rounds))

  override def verify(password: String, hash: String): Boolean =
    blocking(password.isBcryptedBounded(hash))
}


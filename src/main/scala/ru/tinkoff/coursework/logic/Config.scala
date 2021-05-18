package ru.tinkoff.coursework.logic

import pureconfig._
import pureconfig.generic.auto._

import java.io.FileNotFoundException
import scala.concurrent.Future


case class Config(mysqlDB: MySqlDb)


object Config {
  def load(): Future[Config] =
    ConfigSource.default.load[Config] match {
      case Left(_) => Future.failed(new FileNotFoundException)
      case Right(value) => Future.successful(value)
    }
}


case class MySqlDb(driver: String, url: String, user: String, password: String)

package ru.tinkoff.coursework.storage

import ru.tinkoff.coursework.rdbms.DIO
import ru.tinkoff.coursework.logic.AsyncBcryptImpl
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global


case class User(id: Int,
                username: String,
                email: String,
                password: String)


class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
  def userId: Rep[Int] = column("USER_ID", O.PrimaryKey)

  def username: Rep[String] = column("USERNAME")

  def email: Rep[String] = column("EMAIL")

  def password: Rep[String] = column("PASSWORD")

  override def * : ProvenShape[User] =
    (userId, username, email, password) <>
      (User.tupled, User.unapply)
}


object UserQueryRepository {
  val AllUsers = TableQuery[UsersTable]
  val bcrypt = new AsyncBcryptImpl

  def authorize(email: String, password: String): DIO[Option[User], Effect.Read] =
    AllUsers
      .filter { _.email === email }
      .result
      .map {
        _.headOption
          .filter { u => bcrypt.verify(password, u.password) }
      }

  def getUserInfo(email: String): DIO[Option[User], Effect.Read] =
    AllUsers
      .filter { _.email === email }
      .result
      .map { _.headOption }
      .map { _.map { _.copy(password = "*****") } }
}

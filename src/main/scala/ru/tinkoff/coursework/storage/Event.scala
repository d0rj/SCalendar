package ru.tinkoff.coursework.storage

import io.circe.Decoder.Result

import java.sql.Timestamp
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


case class Event(id: String,
                 kind: String,
                 date: Timestamp,
                 duration: Long,
                 title: String,
                 summary: String,
                 location: Option[String],
                 repeating: Boolean,
                 completed: Boolean)


object Event {
  implicit val TimestampFormat : Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeString.apply(a.toString)

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
  }

  implicit val jsonDecoder: Decoder[Event] = deriveDecoder
  implicit val jsonEncoder: Encoder[Event] = deriveEncoder
}

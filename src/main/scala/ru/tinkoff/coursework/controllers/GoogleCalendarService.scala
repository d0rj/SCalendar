package ru.tinkoff.coursework.controllers

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.{Calendar, CalendarScopes}
import ru.tinkoff.coursework.EventNotFoundException
import ru.tinkoff.coursework.logic.GoogleEventConverter
import ru.tinkoff.coursework.storage.{Event, EventsQueryRepository}

import java.io.{File, FileNotFoundException, IOException, InputStreamReader}
import java.sql.Timestamp
import java.util.Collections
import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration.Duration


class GoogleCalendarService extends CalendarService {
  import ru.tinkoff.coursework.logic.GoogleEventConverter._


  private val APPLICATION_NAME = "Calendar App"
  private val JSON_FACTORY = JacksonFactory.getDefaultInstance
  private val TOKENS_DIRECTORY_PATH = "tokens"

  private val SCOPES = Collections.singletonList(CalendarScopes.CALENDAR)
  private val CREDENTIALS_FILE_PATH = "/credentials.json"

  private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport

  private val service = new Calendar.Builder(
    HTTP_TRANSPORT,
    JSON_FACTORY,
    authorize(HTTP_TRANSPORT))
    .setApplicationName(APPLICATION_NAME)
    .build()


  private def authorize(httpTransport: NetHttpTransport): Credential = {
    val in = getClass.getResourceAsStream(CREDENTIALS_FILE_PATH)
    if (in == null)
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))

    val flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport,
      JSON_FACTORY,
      clientSecrets,
      SCOPES)
      .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
      .setAccessType("offline")
      .build
    val receiver = new LocalServerReceiver.Builder().setPort(8888).build

    new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
  }


  override def getEvent(eventId: String): Future[Option[Event]] =
    Future.successful(Some(
      convert(service.events().get("primary", eventId).execute())
    ))


  override def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]] =
    Future.successful(service.events().list("primary")
      .setTimeMin(from)
      .setTimeMax(to)
      .setOrderBy("startTime")
      .setSingleEvents(true)
      .execute
      .getItems.asScala.toSeq.map { convert })


  override def later(from: Timestamp): Future[Seq[Event]] =
    Future.successful(
      service.events().list("primary")
        .setTimeMin(from)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute()
        .getItems.asScala.toSeq.map { convert }
    )


  override def earlier(to: Timestamp): Future[Seq[Event]] =
    Future.successful(
      service.events().list("primary")
        .setTimeMax(to)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute()
        .getItems.asScala.toSeq.map { convert }
    )


  override def newEvent(event: Event): Future[Boolean] =
    Future.successful(
      service.events().insert("primary", GoogleEventConverter.convert(event)).execute().getHtmlLink match {
        case null => false
        case _ => true
      }
    )


  override def updateEvent(eventId: String, updated: Event): Future[Boolean] =
    Future.successful({
      val lastUpdated = service.events().get("primary", eventId).execute().getUpdated
      service.events().update("primary", eventId, convert(updated)).execute().getUpdated != lastUpdated
    })


  override def removeEvent(eventId: String): Future[Boolean] =
    if (eventId.isEmpty)
      Future.successful(false)
    else
      Future.successful(
        try {
          service.events().delete("primary", eventId).execute()
          true
        } catch {
          case _ @ (_: IOException | _: GoogleJsonResponseException) => false
        }
      )


  override def moveEvent(eventId: String, to: Timestamp): Future[Boolean] =
    Future.successful({
      val event = service.events().get("primary", eventId).execute()
      val duration = event.getStart.getDate match {
        case null => event.getEnd.getDateTime.getValue - event.getStart.getDateTime.getValue
        case _ => event.getEnd.getDate.getValue - event.getStart.getDate.getValue
      }
      val newEnd = new Timestamp(to.getValue + duration)
      val newStart = new Timestamp(to.getValue + duration)

      try {
        service.events().update("primary", eventId, event.setStart(newStart).setEnd(newEnd)).execute()
        true
      } catch {
        case _ @ (_: IOException | _: GoogleJsonResponseException) => false
      }
    })

  override def synchronize(calendarId: String, from: Option[Timestamp], to: Option[Timestamp]): Future[Boolean] = {
    val events = (from, to) match {
      case (Some(left), Some(right)) => allBetween(left, right)
      case (Some(left), None) => later(left)
      case (None, Some(right)) => earlier(right)
      case (None, None) => throw new EventNotFoundException
    }

    val db = Database.forConfig("mysqlDB")
    val allEvents = Await.result(db.run(EventsQueryRepository.AllEvents.result), Duration.Inf)

    events
      .map { _.map { e =>
      if (allEvents.exists { _.id == e.id })
        DBIO.sequence(Seq(
          EventsQueryRepository.changeTitle(_, e.title),
          EventsQueryRepository.changeSummary(_, e.summary),
          EventsQueryRepository.changeLocation(_, e.location),
          EventsQueryRepository.changeRepeating(_, e.repeating),
        ).map { _(e.id) })
          .map { _.sum }
      else
        EventsQueryRepository.addEvent(e)}
    }
      .map { _.map { db.run(_) } }
      .flatMap { Future.sequence(_) }
      .map { _.sum }
      .map { _ > 0 }
  }
}

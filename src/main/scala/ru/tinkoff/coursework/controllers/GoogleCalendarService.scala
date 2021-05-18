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
import ru.tinkoff.coursework.ServiceException
import ru.tinkoff.coursework.logic.GoogleEventConverter
import ru.tinkoff.coursework.storage.Event

import java.io.{File, FileNotFoundException, IOException, InputStreamReader}
import java.sql.Timestamp
import java.util.Collections
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class GoogleCalendarService()(implicit ec: ExecutionContext) extends CalendarService with ThirdPartyService {
  import ru.tinkoff.coursework.logic.GoogleEventConverter._
  import scala.jdk.CollectionConverters._


  private val APPLICATION_NAME = "Calendar App"
  private val JSON_FACTORY = JacksonFactory.getDefaultInstance
  private val TOKENS_DIRECTORY_PATH = "tokens"

  private val SCOPES = Collections.singletonList(CalendarScopes.CALENDAR)
  private val CREDENTIALS_FILE_PATH = "/credentials.json"
  private val USER_ID = "user"

  private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport

  private val service = new Calendar.Builder(
    HTTP_TRANSPORT,
    JSON_FACTORY,
    authorize(HTTP_TRANSPORT))
    .setApplicationName(APPLICATION_NAME)
    .build()


  private def authorize(httpTransport: NetHttpTransport): Credential = {
    val in = Option(getClass.getResourceAsStream(CREDENTIALS_FILE_PATH))
    if (in.isEmpty)
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in.get))


    val flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport,
      JSON_FACTORY,
      clientSecrets,
      SCOPES)
      .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
      .setAccessType("offline")
      .build
    val receiver = new LocalServerReceiver.Builder().setPort(8888).build

    new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER_ID)
  }


  private def handleErrors[T]: PartialFunction[Throwable, Future[T]] = {
    case _@(_: IOException | _: GoogleJsonResponseException) => Future.failed(new ServiceException)
  }


  override def getEvent(eventId: String): Future[Event] =
    Future(convert(service.events().get("primary", eventId).execute()))
      .recoverWith { handleErrors }


  override def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]] =
    Future(service.events().list("primary")
      .setTimeMin(from)
      .setTimeMax(to)
      .setOrderBy("startTime")
      .setSingleEvents(true)
      .execute
      .getItems.asScala.toSeq.map { convert }
    )
      .recoverWith { handleErrors }


  override def later(from: Timestamp): Future[Seq[Event]] =
    Future(
      service.events().list("primary")
        .setTimeMin(from)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute()
        .getItems.asScala.toSeq.map { convert }
    )
      .recoverWith { handleErrors }


  override def earlier(to: Timestamp): Future[Seq[Event]] =
    Future(
      service.events().list("primary")
        .setTimeMax(to)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute()
        .getItems.asScala.toSeq.map { convert }
    )
      .recoverWith { handleErrors }


  override def newEvent(event: Event): Future[Unit] =
    Future(service.events().insert("primary", GoogleEventConverter.convert(event)).execute())
      .recoverWith { handleErrors }
      .map { _ => () }


  override def updateEvent(eventId: String, updated: Event): Future[Unit] = {
    val lastUpdated = service.events().get("primary", eventId).execute().getUpdated
    if (service.events().update("primary", eventId, convert(updated)).execute().getUpdated != lastUpdated)
      Future.successful(())
    else
      Future.failed(new ServiceException)
  }


  override def removeEvent(eventId: String): Future[Unit] =
    Future(service.events().delete("primary", eventId).execute())
      .recoverWith { handleErrors }
      .map { _ => () }


  override def moveEvent(eventId: String, to: Timestamp): Future[Unit] = {
    (for {
      event <- Future(service.events().get("primary", eventId).execute())
      ifEmpty = event.getEnd.getDateTime.getValue - event.getStart.getDateTime.getValue
      ifNotEmpty = event.getEnd.getDate.getValue - event.getStart.getDate.getValue
      duration = Try(event.getStart.getDate).toOption.fold(ifEmpty)(_ => ifNotEmpty)
      newEnd = new Timestamp(to.getValue)
      newStart = new Timestamp(to.getValue + duration)
      r <- Future(service.events().update("primary", eventId, event.setStart(newStart).setEnd(newEnd)).execute())
    } yield r)
      .recoverWith { handleErrors }
      .map { _ => () }
  }
}

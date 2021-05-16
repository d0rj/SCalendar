package ru.tinkoff.coursework.controllers

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.{Calendar, CalendarScopes}
import ru.tinkoff.coursework.storage.Event

import java.io.{File, FileNotFoundException, InputStreamReader}
import java.sql.Timestamp
import java.util.Collections
import scala.concurrent.Future


class GoogleCalendarService extends CalendarService {
  private val APPLICATION_NAME = "Calendar App"
  private val JSON_FACTORY = JacksonFactory.getDefaultInstance
  private val TOKENS_DIRECTORY_PATH = "tokens"

  private val SCOPES = Collections.singletonList(CalendarScopes.CALENDAR)
  private val CREDENTIALS_FILE_PATH = "credentials.json"

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


  override def allBetween(from: Timestamp, to: Timestamp): Future[Seq[Event]] = ???

  override def newEvent(event: Event): Future[Boolean] = ???

  override def completeEvent(eventId: String): Future[Boolean] = ???

  override def removeEvent(eventId: String): Future[Boolean] = ???

  override def moveEvent(eventId: String, to: Timestamp): Future[Boolean] = ???
}

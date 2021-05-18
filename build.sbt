
ThisBuild / scalaVersion := "2.13.4"

lazy val circeVersion = "0.13.0"
lazy val tapirVersion = "0.18.0-M4"

ThisBuild / libraryDependencies ++= Seq(
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",

  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,

  "de.heikoseeberger" %% "akka-http-circe" % "1.36.0",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  // slick
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "mysql" % "mysql-connector-java" % "8.0.25",

  // google calendar
  "com.google.apis" % "google-api-services-calendar" % "v3-rev121-1.20.0",
  "com.google.api-client" % "google-api-client-java6" % "1.20.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.20.0",
  "com.google.apis" % "google-api-services-oauth2" % "v2-rev91-1.20.0",

  // migration
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "org.hsqldb" % "hsqldb" % "2.5.0",
  "com.github.pureconfig" %% "pureconfig" % "0.15.0",
)

ThisBuild / scalaVersion := "2.13.4"

lazy val circeVersion = "0.13.0"
lazy val tapirVersion = "0.18.0-M4"

ThisBuild / libraryDependencies ++= Seq(
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",

  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-akka-http" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,

  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.36.0",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "com.softwaremill.macwire" %% "macros" % "2.3.7",
  "com.beachape" %% "enumeratum" % "1.6.1",
  "com.beachape" %% "enumeratum-circe" % "1.6.1",

  // slick
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "com.h2database" % "h2" % "1.4.200",

  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.13" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.4" % Test,
  "org.scalamock" %% "scalamock" % "4.4.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,

  "mysql" % "mysql-connector-java" % "8.0.25"
)

ThisBuild / Test / parallelExecution := false
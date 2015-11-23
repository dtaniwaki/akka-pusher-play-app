name := """akka-pusher-play-app"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  //"io.spray"             %%  "spray-json"    % "1.3.2",
  "com.github.dtaniwaki" %%  "akka-pusher"   % "0.1.4",
  //"com.typesafe.akka"    %%  "akka-slf4j"    % "2.3.14",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

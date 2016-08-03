name := """scala-typesafe-config-tokens"""

version := "0.1"

scalaVersion := "2.11.8"

organization := "org.zalando"

libraryDependencies ++= Seq(
  "org.zalando.stups" % "tokens" % "0.9.9",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "com.iheart" %% "ficus" % "1.2.6",
  "org.specs2" %% "specs2-core" % "3.8.4" % Test
)

publishMavenStyle := true

// Publish snapshots to a different repository
publishTo := {
  val nexus = "https://maven.zalando.net/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots/")
  else
    Some("releases"  at nexus + "content/repositories/releases/")
}

publishArtifact in Test := false

resolvers += Resolver.jcenterRepo
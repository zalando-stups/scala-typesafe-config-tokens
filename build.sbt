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

pomIncludeRepository := { _ => false }

pomExtra := <url>https://github.com/zalando-incubator/scala-typesafe-config-tokens</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>https://opensource.org/licenses/Apache-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/zalando-incubator/scala-typesafe-config-tokens</url>
    <connection>scm:git:git@github.com:zalando-incubator/scala-typesafe-config-tokens.git</connection>
  </scm>
  <developers>
    <developer>
      <id>mdedetrich</id>
      <name>Matthew de Detrich</name>
      <email>matthew.de.detrich@zalando.de</email>
    </developer>
  </developers>

resolvers += Resolver.jcenterRepo
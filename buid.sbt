name := "finch-practice"

version := "0.1"

scalaVersion := "2.12.7"

lazy val finagleHawkVersion = "0.2.1"
lazy val finchSangriaVersion = "0.3.1"
lazy val rbScalaUtilsVersion = "0.1.1"
lazy val circeVersion = "0.10.0"
lazy val catsVersion = "0.9.0"
lazy val catsEffectVersion = "1.0.0"
lazy val mouseVersion = "0.9"
// The version numbers for Finagle, Twitter, Finch & Catbird *must* work together. See the Finch build.sbt for known good versions.
lazy val finagleVersion = "18.9.0"
lazy val finagleHttpAuthVersion = "0.1.0"
lazy val twitterServerVersion = "18.10.0"
lazy val finchVersion = "0.25.0"
lazy val sangriaVersion = "1.2.2"
lazy val sangriaCirceVersion = "1.1.0"
lazy val featherbedVersion = "0.3.1"
lazy val specsVersion = "3.9.4"
lazy val scalaCacheVersion = "0.9.4"
lazy val scalaUriVersion = "0.4.16"
lazy val fetchVersion = "0.6.2"
lazy val slf4jVersion = "1.7.25"
lazy val gattlingVersion = "2.2.5"
lazy val nrVersion = "3.40.0"


resolvers ++= Seq(
  Resolver.jcenterRepo,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Twitter" at "http://maven.twttr.com",
  Resolver.url("bintray-sbt-plugin-releases", url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
  Resolver.bintrayRepo("redbubble", "open-source")
)

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finchx-core" % finchVersion,
  "com.github.finagle" %% "finchx-circe" % finchVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "finagle-stats" % finagleVersion,
  "com.twitter" %% "twitter-server" % twitterServerVersion,
  "com.netaporter" %% "scala-uri" % scalaUriVersion,
  "org.specs2" %% "specs2-core" % specsVersion % "test",
  "org.specs2" %% "specs2-scalacheck" % specsVersion % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.typelevel" %% "cats-laws" % catsVersion % "test",
  "org.typelevel" %% "discipline" % "0.9.0" % "test")
name := "finch-practice"

version := "0.1"

scalaVersion := "2.12.7"

// The version numbers for Finagle, Twitter, Finch & Catbird *must* work together. See the Finch build.sbt for known good versions.
lazy val versions = new {
  val finch           = "0.25.0"
  val finagleHawk     = "0.2.1"
  val finchSangria    = "0.3.1"
  val rbScalaUtils    = "0.1.1"
  val circe           = "0.10.0"
  val cats            = "0.9.0"
  val catsEffect      = "1.0.0"
  val mouse           = "0.9"
  val finagle         = "18.9.0"
  val finagleHttpAuth = "0.1.0"
  val twitterServer   = "18.10.0"
  val sangria         = "1.2.2"
  val sangriaCirce    = "1.1.0"
  val featherbed      = "0.3.1"
  val specs           = "3.9.4"
  val scalaCache      = "0.9.4"
  val scalaUri        = "0.4.16"
  val fetch           = "0.6.2"
  val slf4j           = "1.7.25"
  val gattling        = "2.2.5"
  val nr              = "3.40.0"
  val mockito         = "1.10.19"
  val scalaCheck      = "1.13.5"
  val scalaTest       = "3.0.5"
  val discipline      = "0.9.0"
  val guice           = "4.2.0"
  val logback         = "1.2.3"
  val scalatest       = "3.0.5"
  val junitInterface  = "0.11"
  val dockerItScala   = "0.9.6"
  val hamsters        = "2.6.0"
  val fluentdScala    = "0.2.5"
  val swaggerFinatra  = "18.4.0"
  val wireMock        = "2.17.0"
  val catbird         = "18.5.0"
  val scalaErrors     = "1.2"
  val perfolation     = "1.0.2"
  val cacheRedis      = "0.24.1"
}

resolvers ++= Seq(
  Resolver.jcenterRepo,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Twitter" at "http://maven.twttr.com",
  Resolver.url("bintray-sbt-plugin-releases", url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns
  ),
  Resolver.bintrayRepo("redbubble", "open-source")
)

libraryDependencies ++= Seq(
  "com.github.finagle"           %% "finchx-core"                 % versions.finch,
  "com.github.finagle"           %% "finchx-circe"                % versions.finch,
  "org.typelevel"                %% "cats-effect"                 % versions.catsEffect,
  "org.typelevel"                %% "cats-core"                   % versions.cats,
  "io.circe"                     %% "circe-core"                  % versions.circe,
  "io.circe"                     %% "circe-generic"               % versions.circe,
  "com.twitter"                  %% "finagle-http"                % versions.finagle,
  "com.twitter"                  %% "finagle-stats"               % versions.finagle,
  "com.twitter"                  %% "twitter-server"              % versions.twitterServer,
  "com.netaporter"               %% "scala-uri"                   % versions.scalaUri,
  "org.specs2"                   %% "specs2-core"                 % versions.specs % "test",
  "org.specs2"                   %% "specs2-scalacheck"           % versions.specs % "test",
  "org.mockito"                  % "mockito-all"                  % versions.mockito % "test",
  "org.scalacheck"               %% "scalacheck"                  % versions.scalaCheck % "test",
  "org.scalatest"                %% "scalatest"                   % versions.scalaTest % "test",
  "org.typelevel"                %% "cats-laws"                   % versions.cats % "test",
  "org.typelevel"                %% "discipline"                  % versions.discipline % "test",
  "com.outr"                     %% "perfolation"                 % versions.perfolation,
  "com.github.mehmetakiftutuncu" %% "errors"                      % versions.scalaErrors,
  "io.catbird"                   %% "catbird-finagle"             % versions.catbird,
  "com.github.tomakehurst"       % "wiremock"                     % versions.wireMock,
  "com.jakehschwartz"            % "finatra-swagger_2.12"         % versions.swaggerFinatra,
  "eu.inn"                       %% "fluentd-scala"               % versions.fluentdScala,
  "io.github.scala-hamsters"     %% "hamsters"                    % versions.hamsters,
  "ch.qos.logback"               % "logback-classic"              % versions.logback,
  "com.google.inject.extensions" % "guice-testlib"                % versions.guice % "test",
  "com.whisk"                    %% "docker-testkit-scalatest"    % versions.dockerItScala % "test",
  "com.whisk"                    %% "docker-testkit-impl-spotify" % versions.dockerItScala % "test",
  "com.github.cb372"             %% "scalacache-redis"            % versions.cacheRedis
)

mainClass in Compile := Some("PromoteMain")

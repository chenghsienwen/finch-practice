resolvers += Resolver.url("bintray-sbt-plugin-releases", url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns
)
resolvers += Classpaths.sbtPluginReleases
resolvers += "spray repo" at "http://repo.spray.io"

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("com.github.gseitz"       % "sbt-release"        % "1.0.7")
addSbtPlugin("com.typesafe.sbt"        % "sbt-git"            % "0.9.3")
addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt"         % "0.5.3")
addSbtPlugin("com.geirsson"            % "sbt-scalafmt"       % "1.5.1")
addSbtPlugin("org.duhemm"              % "sbt-errors-summary" % "0.6.0")

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2-RC2")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.2")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.1")

addSbtPlugin("au.com.onegeek" % "sbt-dotenv" % "1.2.58")

// The new relic plugin does not support sbt 1.0, https://github.com/gilt/sbt-newrelic/pull/42
addSbtPlugin(
  "com.gilt.sbt" % "sbt-newrelic" % "0.2.1-3-gfe070f6.jar" from "file:///./project/lib/sbt-newrelic-0.2.1-3-gfe070f6.jar"
)

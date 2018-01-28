name := """captchanator"""

val revision = sys.env.getOrElse("TRAVIS_TAG", sys.props.getOrElse("TRAVIS_TAG", "1.0.0-SNAPSHOT")) match {
  case "" => "1.0.0-SNAPSHOT"
  case v => v
}

version := revision

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtNativePackager)

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-language:reflectiveCalls", "-language:postfixOps", "-language:implicitConversions")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Econic" at "http://repo.enonic.com/public/"
resolvers += Resolver.bintrayRepo("yarosman", "maven")
resolvers += Resolver.jcenterRepo

packageName in Docker := name.value
maintainer in Docker := "yarosman"
version in Docker := version.value
dockerExposedPorts in Docker := Seq(9000)
dockerBaseImage := "anapsix/alpine-java:8_jdk_unlimited"
dockerUpdateLatest := true

libraryDependencies ++= Seq(
  guice,
  "com.iheart" %% "play-swagger" % "0.6.3",
  "com.impactua" %% "redis-scala" % "2.0.0",
  "nl.captcha" % "simplecaptcha" % "1.2.1",
  specs2 % Test,
  "org.specs2" %% "specs2-matcher-extra" % "3.8.9" % Test
)
name := """captchanator"""

val revision = sys.env.getOrElse("TRAVIS_TAG", sys.props.getOrElse("TRAVIS_TAG", "1.0.0-SNAPSHOT")) match {
  case "" => "1.0.0-SNAPSHOT"
  case v => v
}

version := revision

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtNativePackager)

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-language:reflectiveCalls", "-language:postfixOps", "-language:implicitConversions")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += Resolver.bintrayRepo("sergkh", "maven")
resolvers += Resolver.jcenterRepo

packageName in Docker := name.value
maintainer in Docker := "yarosman"
version in Docker := version.value
dockerExposedPorts in Docker := Seq(9000)
dockerBaseImage := "davidcaste/alpine-java-unlimited-jce:jdk8"
dockerUpdateLatest := true

libraryDependencies ++= Seq(
  "com.iheart" %% "play-swagger" % "0.5.3",
  "com.impactua" %% "redis-scala" % "1.3.100",
  "simplecaptcha" % "simplecaptcha" % "1.2.1" from "http://heanet.dl.sourceforge.net/project/simplecaptcha/simplecaptcha-1.2.1.jar",
  specs2 % Test,
  "org.specs2" %% "specs2-matcher-extra" % "3.8.5" % Test
)
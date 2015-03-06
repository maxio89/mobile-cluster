import sbt._

object Dependencies {

  object Version {
    val akka = "2.3.9"
  }

  lazy val frontend = common ++ webjars
  lazy val backend = common ++ metrics

  val common = Seq(
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster" % Version.akka,
    "com.typesafe.akka" %% "akka-kernel" % Version.akka,
    "com.typesafe.akka" %% "akka-contrib" % Version.akka
  )

  val webjars = Seq(
    "org.webjars" % "requirejs" % "2.1.15",
    "org.webjars" % "underscorejs" % "1.7.0-1",
    "org.webjars" % "jquery" % "2.1.3",
    "org.webjars" % "d3js" % "3.4.11",
    "org.webjars" % "nvd3" % "1.1.15-beta-2",
    "org.webjars" % "angular-nvd3" % "0.0.9",
    "org.webjars" % "bootstrap" % "3.3.2" exclude ("org.webjars", "jquery"),
    "org.webjars" % "bootswatch-yeti" % "3.3.2" exclude ("org.webjars", "jquery"),
    "org.webjars" % "angularjs" % "1.3.10" exclude ("org.webjars", "jquery")
  )

  val metrics = Seq(
    "org.fusesource" % "sigar" % "1.6.4"
  )

}

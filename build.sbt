name := "mobile-cluster"

organization in ThisBuild := "pl.edu.agh"

version in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.11.5"

lazy val frontend = (project in file("frontend"))
  .enablePlugins(PlayScala)
  .settings(libraryDependencies ++= (Dependencies.frontend ++ Seq(filters, cache)), pipelineStages := Seq(rjs, digest, gzip),
    RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:")), fork in run := true)
  .dependsOn(api)
  .aggregate(api)

lazy val backend = (project in file("backend"))
  .settings(
    name := "backend",
    libraryDependencies ++= Dependencies.backend,
    javaOptions in run ++= Seq("-Djava.library.path=./sigar"),
    // this enables custom javaOptions
    fork in run := true)
  .dependsOn(api)
  .aggregate(api)

lazy val boot = (project in file("boot"))
  .enablePlugins(AkkaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "boot",
    mainClass in Compile := Some("pl.edu.agh.Boot"),
    libraryDependencies ++= Dependencies.backend,
    // this enables custom javaOptions
    fork in run := true)
  .dependsOn(backend)
  .aggregate(api, backend)

lazy val api = (project in file("api")).settings(name := "api", libraryDependencies ++= Dependencies.backend)

scalacOptions in ThisBuild ++= Seq("-target:jvm-1.7", "-encoding", "UTF-8", "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible", "-Ywarn-dead-code")


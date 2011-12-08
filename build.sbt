organization := "com.novoda"

name := "saab"

version := "0.1.1-SNAPSHOT"

scalacOptions += "-deprecation"

publishMavenStyle := true

publishTo := Some("Scala Tools Nexus" at
                  "http://nexus.scala-tools.org/content/repositories/releases/")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

libraryDependencies ++= Seq(
  "com.google.android.tools" % "ddmlib" % "r13",
  "com.android" % "sdklib" % "r4"  from "http://dl.dropbox.com/u/615212/jars/sdklib.jar",
  "org.specs2" %% "specs2" % "1.6.1" % "test"
)

sbtPlugin := true

seq(ScriptedPlugin.scriptedSettings: _*)

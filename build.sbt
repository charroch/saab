organization := "com.novoda"

name := "saab"

version := "0.1.0-SNAPSHOT"

scalacOptions += "-deprecation"

publishMavenStyle := true

publishTo := Some("Scala Tools Nexus" at
                  "http://nexus.scala-tools.org/content/repositories/releases/")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

libraryDependencies ++= Seq(
  "com.google.android.tools" % "ddmlib" % "r13",
  "org.specs2" %% "specs2" % "1.6.1" % "test"
)

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-proguard-plugin" % (v+"-0.1.1"))

sbtPlugin := true

seq(ScriptedPlugin.scriptedSettings: _*)

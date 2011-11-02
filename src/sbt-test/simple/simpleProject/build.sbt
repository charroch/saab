
import saab.Plugin._

organization := "org.scalatools.sbt"

name := "library"

version := "0.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

seq(defaultSettings : _*)

//platformName := "android-13"


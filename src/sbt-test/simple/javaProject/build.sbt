
import saab.Plugin._

organization := "org.sbt.android"

name := "library"

version := "0.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

seq(defaultSettings : _*)

seq(adb.globalSettings : _*)

//platformName := "android-13"


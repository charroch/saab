
import saab.Plugin._

organization := "org.sbt.android"

name := "library"

version := "0.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1"

libraryDependencies += "com.google.code.gson" % "gson" % "1.7.1"

seq(defaultSettings : _*)

seq(adb.globalSettings : _*)

//platformName := "android-13"

